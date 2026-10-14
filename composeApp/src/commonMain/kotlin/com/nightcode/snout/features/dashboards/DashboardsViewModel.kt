package com.nightcode.snout.features.dashboards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.model.insights.DashboardSummary
import com.nightcode.snout.shared.domain.usecase.appstate.ProjectReload
import com.nightcode.snout.shared.domain.usecase.insights.GetDashboards
import com.nightcode.snout.shared.domain.usecase.insights.ObserveDashboardReloads
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class DashboardsViewModel(
    private val getDashboards: GetDashboards,
    observeDashboardReloads: ObserveDashboardReloads,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardsUiState())
    val uiState: StateFlow<DashboardsUiState> = _uiState.asStateFlow()

    private var lastReload: ProjectReload? = null
    private var loadJob: Job? = null
    private var hasEntered = false

    init {
        viewModelScope.launch {
            observeDashboardReloads().collect { reload ->
                // Another project's dashboards start from the skeleton; the same project's stay on screen meanwhile.
                if (reload != lastReload) _uiState.update { it.copy(dashboards = null) }
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
            _uiState.update { it.copy(isRefreshing = it.dashboards != null, error = null) }
            val result = getDashboards().map { it.sortedForDisplay() }
            _uiState.update { state ->
                result.fold(
                    ifLeft = { state.copy(isRefreshing = false, error = it) },
                    ifRight = { state.copy(dashboards = it, isRefreshing = false, error = null) },
                )
            }
        }
    }
}

private fun List<DashboardSummary>.sortedForDisplay() = sortedWith(
    compareByDescending<DashboardSummary> { it.pinned }.thenByDescending { it.lastAccessedAt }.thenBy { it.name },
)
