package com.nightcode.snout.features.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.model.insights.InsightSummary
import com.nightcode.snout.shared.domain.usecase.insights.GetInsights
import com.nightcode.snout.shared.domain.usecase.insights.ObserveInsightReloads
import com.nightcode.snout.shared.domain.usecase.insights.SearchInsights
import com.nightcode.snout.shared.domain.usecase.insights.SetInsightFavorite
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@KoinViewModel
internal class InsightsViewModel(
    private val getInsights: GetInsights,
    private val searchInsights: SearchInsights,
    private val setInsightFavorite: SetInsightFavorite,
    observeInsightReloads: ObserveInsightReloads,
    searchDebounce: Duration = 300.milliseconds,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<InsightsSideEffect>()
    val sideEffects: Flow<InsightsSideEffect> = _sideEffects.receiveAsFlow()

    private var loadJob: Job? = null
    private var hasEntered = false

    init {
        viewModelScope.launch {
            // A favorite changed here or on the detail screen reloads the list through the insight reloads.
            observeInsightReloads().collect { reload ->
                // Another project's insights start from the skeleton; the same project's stay on screen meanwhile.
                if (reload.projectChanged) _uiState.update { it.copy(insights = null) }
                load()
            }
        }
        viewModelScope.launch {
            // Searches once typing pauses, cancelling the search for the previous query.
            _uiState.map { it.query }.distinctUntilChanged()
                .debounce(searchDebounce).distinctUntilChanged()
                .collectLatest { search(it) }
        }
    }

    fun onQueryChanged(text: String) {
        _uiState.update { it.copy(query = text) }
    }

    fun refresh() = load()

    /** Refreshes each time the screen is entered again, but not the first time nor while a load runs. */
    fun onScreenEntered() {
        if (hasEntered && loadJob?.isActive != true) refresh()
        hasEntered = true
    }

    fun toggleFavorite(insight: InsightSummary) = viewModelScope.launch {
        setInsightFavorite(insight.id, insight.favorited.not())
            .onLeft { _sideEffects.send(InsightsSideEffect.FavoriteUpdateFailed) }
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = it.insights != null, error = null) }
            val result = getInsights()
            _uiState.update { state ->
                result.fold(
                    ifLeft = { state.copy(isRefreshing = false, error = it) },
                    ifRight = { state.copy(insights = it, isRefreshing = false, error = null) },
                )
            }
        }
    }

    private suspend fun search(query: String) {
        _uiState.update { it.copy(searchResults = null, searchError = null) }
        if (query.isBlank()) return
        val result = searchInsights(query)
        _uiState.update { state ->
            result.fold(
                ifLeft = { state.copy(searchError = it) },
                ifRight = { state.copy(searchResults = it) },
            )
        }
    }
}
