package com.nightcode.snout.features.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.RefreshableUiState
import com.nightcode.snout.common.state.loadRefreshableList
import com.nightcode.snout.common.state.searchResults
import com.nightcode.snout.shared.domain.model.insights.InsightSummary
import com.nightcode.snout.shared.domain.usecase.insights.GetInsights
import com.nightcode.snout.shared.domain.usecase.insights.ObserveInsightReloads
import com.nightcode.snout.shared.domain.usecase.insights.SearchInsights
import com.nightcode.snout.shared.domain.usecase.insights.SetInsightFavorite
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@KoinViewModel
internal class InsightsViewModel(
    private val searchInsights: SearchInsights,
    private val setInsightFavorite: SetInsightFavorite,
    getInsights: GetInsights,
    observeInsightReloads: ObserveInsightReloads,
    searchDebounce: Duration = 300.milliseconds,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val refreshes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val _sideEffects = Channel<InsightsSideEffect>()
    val sideEffects: Flow<InsightsSideEffect> = _sideEffects.receiveAsFlow()

    // A favorite changed here or on the detail screen reloads the list through the insight reloads.
    private val list = observeInsightReloads().loadRefreshableList(viewModelScope, refreshes) { getInsights() }

    private val searchResults = query.searchResults(searchDebounce) { searchInsights(it) }

    val uiState: StateFlow<InsightsUiState> = combine(query, list, searchResults) { query, list, searched ->
        InsightsUiState(query = query, insights = if (query.isBlank()) list else searched ?: RefreshableUiState.Loading)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightsUiState())

    fun onQueryChanged(text: String) {
        query.value = text
    }

    fun refresh() {
        refreshes.tryEmit(Unit)
    }

    fun toggleFavorite(insight: InsightSummary) = viewModelScope.launch {
        setInsightFavorite(insight.id, insight.favorited.not())
            .onLeft { _sideEffects.send(InsightsSideEffect.FavoriteUpdateFailed) }
    }
}
