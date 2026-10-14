package com.nightcode.snout.features.activitylog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.usecase.activitylog.GetActivityLog
import com.nightcode.snout.shared.domain.usecase.activitylog.ObserveActivityLogReloads
import com.nightcode.snout.shared.domain.usecase.appstate.ProjectReload
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class ActivityLogViewModel(
    private val getActivityLog: GetActivityLog,
    observeActivityLogReloads: ObserveActivityLogReloads,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityLogUiState())
    val uiState: StateFlow<ActivityLogUiState> = _uiState.asStateFlow()

    private var lastReload: ProjectReload? = null
    private var loadJob: Job? = null
    private var hasEntered = false

    init {
        viewModelScope.launch {
            observeActivityLogReloads().collect { reload ->
                // Another project's entries start from the skeleton; the same project's stay on screen meanwhile.
                if (reload != lastReload) _uiState.update { it.copy(entries = null) }
                lastReload = reload
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
            _uiState.update { it.copy(isRefreshing = it.entries != null, error = null) }
            val result = getActivityLog()
            _uiState.update { state ->
                result.fold(
                    ifLeft = { state.copy(isRefreshing = false, error = it) },
                    ifRight = { state.copy(entries = it, isRefreshing = false, error = null) },
                )
            }
        }
    }
}
