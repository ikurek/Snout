package com.nightcode.snout.features.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.model.logs.LogsRange
import com.nightcode.snout.shared.domain.usecase.appstate.ProjectReload
import com.nightcode.snout.shared.domain.usecase.logs.GetLogServices
import com.nightcode.snout.shared.domain.usecase.logs.ObserveLogServiceReloads
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class LogsViewModel(
    private val getServices: GetLogServices,
    observeLogServiceReloads: ObserveLogServiceReloads,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogsUiState())
    val uiState: StateFlow<LogsUiState> = _uiState.asStateFlow()

    private var lastReload: ProjectReload? = null
    private var loadJob: Job? = null
    private var hasEntered = false

    init {
        viewModelScope.launch {
            observeLogServiceReloads().collect { reload ->
                // Another project's services start from the skeleton; the same project's stay on screen meanwhile.
                if (reload != lastReload) _uiState.update { it.copy(services = null) }
                lastReload = reload
                load()
            }
        }
    }

    fun onRangeSelected(value: LogsRange) {
        if (value == _uiState.value.range) return
        // Another range starts from the skeleton: the services shown so far counted another period.
        _uiState.update { it.copy(range = value, services = null) }
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
            _uiState.update { it.copy(isRefreshing = it.services != null, error = null) }
            val result = getServices(_uiState.value.range)
            _uiState.update { state ->
                result.fold(
                    ifLeft = { state.copy(isRefreshing = false, error = it) },
                    ifRight = { state.copy(services = it, isRefreshing = false, error = null) },
                )
            }
        }
    }
}
