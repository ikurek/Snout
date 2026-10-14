package com.nightcode.snout.features.errortracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter
import com.nightcode.snout.shared.domain.usecase.appstate.ProjectReload
import com.nightcode.snout.shared.domain.usecase.errortracking.GetErrorAppVersions
import com.nightcode.snout.shared.domain.usecase.errortracking.GetErrorTrackingIssues
import com.nightcode.snout.shared.domain.usecase.errortracking.ObserveErrorTrackingReloads
import com.nightcode.snout.shared.domain.usecase.errortracking.SearchErrorTrackingIssues
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@KoinViewModel
internal class ErrorTrackingViewModel(
    private val getIssues: GetErrorTrackingIssues,
    private val searchIssues: SearchErrorTrackingIssues,
    private val getAppVersions: GetErrorAppVersions,
    observeErrorTrackingReloads: ObserveErrorTrackingReloads,
    searchDebounce: Duration = 300.milliseconds,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ErrorTrackingUiState())
    val uiState: StateFlow<ErrorTrackingUiState> = _uiState.asStateFlow()

    private var lastReload: ProjectReload? = null

    /** The query once typing paused; filtered issues are fetched for it rather than for each keystroke. */
    private var settledQuery = ""
    private var loadJob: Job? = null
    private var filteredLoadJob: Job? = null
    private var appVersionsJob: Job? = null
    private var hasEntered = false

    init {
        viewModelScope.launch {
            // A status changed in the app reloads the list it left, through the error tracking reloads.
            observeErrorTrackingReloads().collect { reload ->
                if (reload != lastReload) {
                    // Another project's issues start from the skeleton, and its versions differ, so the version
                    // filter goes too.
                    _uiState.update { it.copy(issues = null, appVersion = null, appVersions = emptyList()) }
                    appVersionsJob?.cancel()
                    appVersionsJob = viewModelScope.launch { loadAppVersions() }
                }
                lastReload = reload
                load()
                loadFiltered()
            }
        }
        viewModelScope.launch {
            _uiState.map { it.query }.distinctUntilChanged()
                .debounce(searchDebounce).distinctUntilChanged()
                .collect { query ->
                    settledQuery = query
                    loadFiltered()
                }
        }
    }

    fun onQueryChanged(text: String) {
        _uiState.update { it.copy(query = text) }
    }

    fun onStatusChanged(value: IssueStatusFilter) {
        if (value == _uiState.value.status) return
        // Another status starts from the skeleton: the issues on screen have the previous one.
        _uiState.update { it.copy(status = value, issues = null) }
        load()
        loadFiltered()
    }

    fun onAppVersionChanged(value: String?) {
        if (value == _uiState.value.appVersion) return
        _uiState.update { it.copy(appVersion = value) }
        loadFiltered()
    }

    fun refresh() {
        viewModelScope.launch { loadAppVersions() }
        if (_uiState.value.isFiltered) loadFiltered() else load()
    }

    /** Refreshes each time the screen is entered again, but not the first time nor while the list loads. */
    fun onScreenEntered() {
        if (hasEntered && loadJob?.isActive != true) refresh()
        hasEntered = true
    }

    /** Loads the issues with the selected status, unfiltered. */
    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = it.issues != null, error = null) }
            val result = getIssues(_uiState.value.status)
            _uiState.update { state ->
                result.fold(
                    ifLeft = { state.copy(isRefreshing = false, error = it) },
                    ifRight = { state.copy(issues = it, isRefreshing = false, error = null) },
                )
            }
        }
    }

    /** Loads the issues matching the query and app version, from the skeleton; clears them when neither is set. */
    private fun loadFiltered() {
        filteredLoadJob?.cancel()
        _uiState.update { it.copy(filteredIssues = null, filterError = null) }
        val state = _uiState.value
        if (settledQuery.isBlank() && state.appVersion == null) return
        filteredLoadJob = viewModelScope.launch {
            val result = searchIssues(state.status, settledQuery, state.appVersion)
            _uiState.update {
                result.fold(
                    ifLeft = { error -> it.copy(filterError = error) },
                    ifRight = { issues -> it.copy(filteredIssues = issues) },
                )
            }
        }
    }

    /** A failure only hides the version filter; the issue list reports its own errors. */
    private suspend fun loadAppVersions() {
        getAppVersions().onRight { versions -> _uiState.update { it.copy(appVersions = versions) } }
    }
}
