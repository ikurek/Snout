package com.nightcode.snout.features.widgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.usecase.appstate.IsSignedIn
import com.nightcode.snout.shared.domain.usecase.insights.GetInsights
import com.nightcode.snout.shared.domain.usecase.insights.SearchInsights
import com.nightcode.snout.widgets.WidgetProjects
import com.nightcode.snout.widgets.WidgetState
import com.nightcode.snout.widgets.fitsWidget
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Picks the insight an Android insight widget shows: a project (none for a new widget, which the person picks), then
 * one of its insights as the Insights tab lists and searches them, without favorites, and without the
 * insights a widget can't show.
 */
@OptIn(FlowPreview::class)
@KoinViewModel
internal class InsightWidgetPickerViewModel(
    @InjectedParam private val widget: WidgetProject,
    private val isSignedIn: IsSignedIn,
    private val widgetProjects: WidgetProjects,
    private val getInsights: GetInsights,
    private val searchInsights: SearchInsights,
    searchDebounce: Duration = 300.milliseconds,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightWidgetPickerUiState())
    val uiState: StateFlow<InsightWidgetPickerUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            // A widget can be added before Snout was ever opened, or after logging out: there is nothing to pick then.
            val isConnected = isSignedIn()
            _uiState.update { it.copy(isConnected = isConnected) }
            if (!isConnected) return@launch
            val projectId = widget.projectId
            _uiState.update { it.copy(projectId = projectId) }
            if (projectId != null) load()
            loadProjects()
        }
        viewModelScope.launch {
            // Searches once typing pauses, cancelling the search for the previous query or project.
            _uiState.map { it.query to it.projectId }.distinctUntilChanged()
                .debounce(searchDebounce).distinctUntilChanged()
                .collectLatest { (query, projectId) -> search(query, projectId) }
        }
    }

    fun onQueryChanged(text: String) {
        _uiState.update { it.copy(query = text) }
    }

    fun onProjectSelected(projectId: Long) {
        if (projectId == _uiState.value.projectId) return
        // Another project starts on its whole list: the search for the previous one doesn't carry over.
        _uiState.update {
            it.copy(
                projectId = projectId,
                insights = null,
                error = null,
                query = "",
                searchResults = null,
                searchError = null,
            )
        }
        load()
    }

    fun refresh() = load()

    /** Loads the projects again, after they failed to. */
    fun retryProjects() {
        viewModelScope.launch { loadProjects() }
    }

    private suspend fun loadProjects() {
        _uiState.update { it.copy(projectsError = null) }
        val projects = widgetProjects.projects()
        _uiState.update {
            when (projects) {
                is WidgetState.Loaded -> it.copy(projects = projects.content)

                is WidgetState.Failed -> it.copy(projectsError = projects.error)

                // Signed out since the screen opened: nothing to pick, as when it opens signed out.
                WidgetState.SignedOut -> it.copy(isConnected = false)
            }
        }
    }

    private fun load() {
        val projectId = _uiState.value.projectId ?: return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = it.insights != null, error = null) }
            val result = getInsights(projectId)
            _uiState.update { state ->
                result.fold(
                    ifLeft = { state.copy(isRefreshing = false, error = it) },
                    ifRight = { insights ->
                        state.copy(insights = insights.filter { it.fitsWidget }, isRefreshing = false, error = null)
                    },
                )
            }
        }
    }

    private suspend fun search(query: String, projectId: Long?) {
        _uiState.update { it.copy(searchResults = null, searchError = null) }
        if (query.isBlank() || projectId == null) return
        val result = searchInsights(query, projectId)
        _uiState.update { state ->
            // Another project or query since: these results belong to neither.
            if (state.projectId != projectId || state.query != query) return@update state
            result.fold(
                ifLeft = { state.copy(searchError = it) },
                ifRight = { insights -> state.copy(searchResults = insights.filter { it.fitsWidget }) },
            )
        }
    }
}
