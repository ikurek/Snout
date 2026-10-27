package com.nightcode.snout.features.insights.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.usecase.insights.GetInsightDetail
import com.nightcode.snout.shared.domain.usecase.insights.SetInsightFavorite
import com.nightcode.snout.shared.domain.usecase.telemetry.TrackInsightViewed
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class InsightDetailViewModel(
    @InjectedParam private val insightId: Long,
    private val getInsightDetail: GetInsightDetail,
    private val setInsightFavorite: SetInsightFavorite,
    private val trackInsightViewed: TrackInsightViewed,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightDetailUiState())
    val uiState: StateFlow<InsightDetailUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<InsightDetailSideEffect>()
    val sideEffects: Flow<InsightDetailSideEffect> = _sideEffects.receiveAsFlow()

    private var loadJob: Job? = null
    private var hasEntered = false
    private var hasReportedView = false

    init {
        load()
    }

    fun refresh() = load()

    /** Refreshes each time the screen is entered again, but not the first time nor while a load runs. */
    fun onScreenEntered() {
        if (hasEntered && loadJob?.isActive != true) refresh()
        hasEntered = true
    }

    fun toggleFavorite() = viewModelScope.launch {
        val current = _uiState.value.insight ?: return@launch
        val target = current.summary.favorited.not()
        updateFavorited(target)
        setInsightFavorite(insightId, target).onLeft {
            updateFavorited(target.not())
            _sideEffects.send(InsightDetailSideEffect.FavoriteUpdateFailed)
        }
    }

    private fun updateFavorited(favorited: Boolean) = _uiState.update { state ->
        state.copy(insight = state.insight?.let { it.copy(summary = it.summary.copy(favorited = favorited)) })
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = it.insight != null, error = null) }
            val result = getInsightDetail(insightId)
            _uiState.update { state ->
                result.fold(
                    ifLeft = { state.copy(isRefreshing = false, error = it) },
                    ifRight = { state.copy(insight = it, isRefreshing = false, error = null) },
                )
            }
            // Once per visit: refreshes aren't views.
            result.onRight { if (!hasReportedView) trackInsightViewed(it.summary.kind).also { hasReportedView = true } }
        }
    }
}
