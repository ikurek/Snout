package com.nightcode.snout.features.experiments.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.usecase.experiments.GetExperiment
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class ExperimentDetailViewModel(
    @InjectedParam private val experimentId: Long,
    private val getExperiment: GetExperiment,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExperimentDetailUiState())
    val uiState: StateFlow<ExperimentDetailUiState> = _uiState.asStateFlow()

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

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = it.experiment != null, error = null) }
            val result = getExperiment(experimentId)
            _uiState.update { state ->
                result.fold(
                    ifLeft = { state.copy(isRefreshing = false, error = it) },
                    ifRight = { state.copy(experiment = it, isRefreshing = false, error = null) },
                )
            }
        }
    }
}
