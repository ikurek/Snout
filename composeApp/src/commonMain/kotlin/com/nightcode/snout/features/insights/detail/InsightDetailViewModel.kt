package com.nightcode.snout.features.insights.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.shared.domain.model.insights.InsightDetail
import com.nightcode.snout.shared.domain.usecase.insights.GetInsightDetail
import com.nightcode.snout.shared.domain.usecase.insights.SetInsightFavorite
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

internal sealed interface InsightDetailSideEffect {
    data object FavoriteUpdateFailed : InsightDetailSideEffect
}

@KoinViewModel
internal class InsightDetailViewModel(
    @InjectedParam private val insightId: Long,
    private val getInsightDetail: GetInsightDetail,
    private val setInsightFavorite: SetInsightFavorite,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CachedUiState<InsightDetail>>(CachedUiState.Loading)
    val uiState: StateFlow<CachedUiState<InsightDetail>> = _uiState.asStateFlow()

    private val _sideEffects = Channel<InsightDetailSideEffect>()
    val sideEffects: Flow<InsightDetailSideEffect> = _sideEffects.receiveAsFlow()

    init {
        load()
    }

    fun load() = viewModelScope.launch {
        _uiState.value = CachedUiState.Loading
        _uiState.value = getInsightDetail(insightId).fold(
            ifLeft = { CachedUiState.Error(it) },
            ifRight = { CachedUiState.Content(it, isRefreshing = false) },
        )
    }

    fun onFavoriteToggle() = viewModelScope.launch {
        val current = (_uiState.value as? CachedUiState.Content)?.data ?: return@launch
        val target = current.summary.favorited.not()
        updateFavorited(target)
        setInsightFavorite(insightId, target).onLeft {
            updateFavorited(target.not())
            _sideEffects.send(InsightDetailSideEffect.FavoriteUpdateFailed)
        }
    }

    private fun updateFavorited(favorited: Boolean) = _uiState.update { state ->
        if (state is CachedUiState.Content) {
            state.copy(data = state.data.copy(summary = state.data.summary.copy(favorited = favorited)))
        } else {
            state
        }
    }
}
