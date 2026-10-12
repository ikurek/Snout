package com.nightcode.snout.features.featureflags.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.LoadState
import com.nightcode.snout.common.state.toLoadState
import com.nightcode.snout.shared.domain.usecase.featureflags.GetFeatureFlag
import com.nightcode.snout.shared.domain.usecase.featureflags.SetFeatureFlagActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class FeatureFlagDetailViewModel(
    @InjectedParam private val flagId: Long,
    private val getFeatureFlag: GetFeatureFlag,
    private val setFeatureFlagActive: SetFeatureFlagActive,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeatureFlagDetailUiState())
    val uiState: StateFlow<FeatureFlagDetailUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        _uiState.update { it.copy(flag = LoadState.Loading) }
        val flag = getFeatureFlag(flagId).toLoadState()
        _uiState.update { it.copy(flag = flag) }
    }

    fun onToggleConfirmed(active: Boolean) = viewModelScope.launch {
        _uiState.update { it.copy(toggle = ToggleStatus.Updating) }
        setFeatureFlagActive(flagId, active).fold(
            ifLeft = { error -> _uiState.update { it.copy(toggle = ToggleStatus.Failed(error)) } },
            ifRight = { updated ->
                _uiState.update {
                    it.copy(flag = LoadState.Loaded(updated), toggle = ToggleStatus.Idle)
                }
            },
        )
    }

    fun onToggleErrorDismissed() = _uiState.update { it.copy(toggle = ToggleStatus.Idle) }
}
