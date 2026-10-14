package com.nightcode.snout.features.experiments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveProjectReloads
import com.nightcode.snout.shared.domain.usecase.experiments.GetExperiments
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class ExperimentsViewModel(
    private val getExperiments: GetExperiments,
    observeProjectReloads: ObserveProjectReloads,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExperimentsUiState())
    val uiState: StateFlow<ExperimentsUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var hasEntered = false

    init {
        viewModelScope.launch {
            observeProjectReloads().collect { reload ->
                // Another project's experiments start from the skeleton; the same project's stay on screen meanwhile.
                if (reload.projectChanged) _uiState.update { it.copy(experiments = null) }
                load()
            }
        }
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
            _uiState.update { it.copy(isRefreshing = it.experiments != null, error = null) }
            val result = getExperiments().map { experiments -> experiments.filterNot { it.archived } }
            _uiState.update { state ->
                result.fold(
                    ifLeft = { state.copy(isRefreshing = false, error = it) },
                    ifRight = { state.copy(experiments = it, isRefreshing = false, error = null) },
                )
            }
        }
    }
}
