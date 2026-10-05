package com.nightcode.snout.features.dashboards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.common.state.RefreshController
import com.nightcode.snout.common.state.listUiState
import com.nightcode.snout.shared.domain.model.insights.DashboardSummary
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.insights.ObserveDashboards
import com.nightcode.snout.shared.domain.usecase.insights.RefreshDashboards
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class DashboardsViewModel(
    private val refreshDashboards: RefreshDashboards,
    observeDashboards: ObserveDashboards,
    observeSelectedProjectId: ObserveSelectedProjectId,
) : ViewModel() {

    private val refresh = RefreshController()

    val uiState: StateFlow<CachedUiState<List<DashboardSummary>>> = combine(
        observeDashboards().map { dashboards -> dashboards?.sortedForDisplay() },
        refresh.status,
    ) { dashboards, status -> listUiState(dashboards, status) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CachedUiState.Loading)

    init {
        viewModelScope.launch {
            observeSelectedProjectId().collectLatest { refresh.run { refreshDashboards() } }
        }
    }

    fun refresh() = viewModelScope.launch { refresh.run { refreshDashboards() } }
}

private fun List<DashboardSummary>.sortedForDisplay() = sortedWith(
    compareByDescending<DashboardSummary> { it.pinned }.thenByDescending { it.lastAccessedAt }.thenBy { it.name },
)
