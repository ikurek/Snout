package com.nightcode.snout.features.insights.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.LoadState
import com.nightcode.snout.common.state.dataOrNull
import com.nightcode.snout.common.state.toLoadState
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

@KoinViewModel
internal class InsightDetailViewModel(
    @InjectedParam private val insightId: Long,
    private val getInsightDetail: GetInsightDetail,
    private val setInsightFavorite: SetInsightFavorite,
) : ViewModel() {

    private val _uiState = MutableStateFlow<InsightDetailUiState>(LoadState.Loading)
    val uiState: StateFlow<InsightDetailUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<InsightDetailSideEffect>()
    val sideEffects: Flow<InsightDetailSideEffect> = _sideEffects.receiveAsFlow()

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        _uiState.value = LoadState.Loading
        _uiState.value = getInsightDetail(insightId).toLoadState()
    }

    fun toggleFavorite() = viewModelScope.launch {
        val current = _uiState.value.dataOrNull ?: return@launch
        val target = current.summary.favorited.not()
        updateFavorited(target)
        setInsightFavorite(insightId, target).onLeft {
            updateFavorited(target.not())
            _sideEffects.send(InsightDetailSideEffect.FavoriteUpdateFailed)
        }
    }

    private fun updateFavorited(favorited: Boolean) = _uiState.update { state ->
        if (state is LoadState.Loaded) {
            state.copy(data = state.data.copy(summary = state.data.summary.copy(favorited = favorited)))
        } else {
            state
        }
    }
}
