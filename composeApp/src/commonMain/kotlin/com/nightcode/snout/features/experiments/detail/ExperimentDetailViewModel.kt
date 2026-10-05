package com.nightcode.snout.features.experiments.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.shared.domain.model.experiments.Experiment
import com.nightcode.snout.shared.domain.usecase.experiments.GetExperiment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class ExperimentDetailViewModel(
    @InjectedParam private val experimentId: Long,
    private val getExperiment: GetExperiment,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CachedUiState<Experiment>>(CachedUiState.Loading)
    val uiState: StateFlow<CachedUiState<Experiment>> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() = viewModelScope.launch {
        _uiState.value = CachedUiState.Loading
        _uiState.value = getExperiment(experimentId).fold(
            ifLeft = { CachedUiState.Error(it) },
            ifRight = { CachedUiState.Content(it, isRefreshing = false) },
        )
    }
}
