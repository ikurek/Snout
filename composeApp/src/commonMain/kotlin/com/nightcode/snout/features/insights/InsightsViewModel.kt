package com.nightcode.snout.features.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.common.state.RefreshController
import com.nightcode.snout.common.state.RefreshStatus
import com.nightcode.snout.common.state.listUiState
import com.nightcode.snout.shared.domain.model.insights.InsightSummary
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.insights.ObserveInsights
import com.nightcode.snout.shared.domain.usecase.insights.RefreshInsights
import com.nightcode.snout.shared.domain.usecase.insights.SearchInsights
import com.nightcode.snout.shared.domain.usecase.insights.SetInsightFavorite
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal sealed interface InsightsSideEffect {
    data object FavoriteUpdateFailed : InsightsSideEffect
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@KoinViewModel
internal class InsightsViewModel(
    private val refreshInsights: RefreshInsights,
    private val searchInsights: SearchInsights,
    private val setInsightFavorite: SetInsightFavorite,
    observeInsights: ObserveInsights,
    observeSelectedProjectId: ObserveSelectedProjectId,
    searchDebounce: Duration = 300.milliseconds,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val refresh = RefreshController()
    private val _sideEffects = Channel<InsightsSideEffect>()
    val sideEffects: Flow<InsightsSideEffect> = _sideEffects.receiveAsFlow()

    private val cachedList = combine(
        observeInsights(),
        refresh.status,
    ) { insights, status -> listUiState(insights, status) }

    private val searchResults: Flow<CachedUiState<List<InsightSummary>>?> = query
        .debounce(searchDebounce)
        .distinctUntilChanged()
        .flatMapLatest { text -> if (text.isBlank()) flowOf(null) else search(text) }

    val uiState: StateFlow<InsightsUiState> = combine(query, cachedList, searchResults) { query, cached, searched ->
        InsightsUiState(query = query, insights = if (query.isBlank()) cached else searched ?: CachedUiState.Loading)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightsUiState())

    init {
        viewModelScope.launch {
            observeSelectedProjectId().collectLatest { refresh.run { refreshInsights() } }
        }
    }

    fun onQueryChange(text: String) {
        query.value = text
    }

    fun refresh() = viewModelScope.launch { refresh.run { refreshInsights() } }

    fun onFavoriteToggle(insight: InsightSummary) = viewModelScope.launch {
        setInsightFavorite(insight.id, insight.favorited.not())
            .onLeft { _sideEffects.send(InsightsSideEffect.FavoriteUpdateFailed) }
    }

    private fun search(text: String): Flow<CachedUiState<List<InsightSummary>>> = flow {
        emit(CachedUiState.Loading)
        emit(
            searchInsights(text).fold(
                ifLeft = { CachedUiState.Error(it) },
                ifRight = { listUiState(it, RefreshStatus.Idle) },
            ),
        )
    }
}
