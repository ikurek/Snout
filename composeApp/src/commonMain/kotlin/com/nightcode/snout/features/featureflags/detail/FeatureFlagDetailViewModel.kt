package com.nightcode.snout.features.featureflags.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.usecase.featureflags.GetFeatureFlag
import com.nightcode.snout.shared.domain.usecase.featureflags.SetFeatureFlagActive
import com.nightcode.snout.shared.domain.usecase.telemetry.TrackFeatureFlagToggled
import kotlinx.coroutines.Job
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
    private val trackFeatureFlagToggled: TrackFeatureFlagToggled,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeatureFlagDetailUiState())
    val uiState: StateFlow<FeatureFlagDetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var hasEntered = false

    init {
        load()
    }

    fun refresh() = load()

    /** Refreshes each time the screen is entered again, but not the first time nor while a load runs. */
    fun onScreenEntered() {
        if (hasEntered && loadJob?.isActive != true) refresh()
        hasEntered = true
    }

    fun onToggleConfirmed(active: Boolean) = viewModelScope.launch {
        _uiState.update { it.copy(toggle = ToggleStatus.Updating) }
        setFeatureFlagActive(flagId, active).fold(
            ifLeft = { error -> _uiState.update { it.copy(toggle = ToggleStatus.Failed(error)) } },
            ifRight = { updated ->
                _uiState.update {
                    it.copy(flag = updated, error = null, isRefreshing = false, toggle = ToggleStatus.Idle)
                }
                trackFeatureFlagToggled(active)
            },
        )
    }

    fun onToggleErrorDismissed() = _uiState.update { it.copy(toggle = ToggleStatus.Idle) }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = it.flag != null, error = null) }
            val result = getFeatureFlag(flagId)
            _uiState.update { state ->
                result.fold(
                    ifLeft = { state.copy(isRefreshing = false, error = it) },
                    ifRight = { state.copy(flag = it, isRefreshing = false, error = null) },
                )
            }
        }
    }
}
