package com.nightcode.snout.features.dashboards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.RefreshableUiState
import com.nightcode.snout.common.state.loadRefreshableList
import com.nightcode.snout.shared.domain.model.insights.DashboardSummary
import com.nightcode.snout.shared.domain.usecase.insights.GetDashboards
import com.nightcode.snout.shared.domain.usecase.insights.ObserveDashboardReloads
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class DashboardsViewModel(getDashboards: GetDashboards, observeDashboardReloads: ObserveDashboardReloads) :
    ViewModel() {

    private val refreshes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val uiState: StateFlow<RefreshableUiState<List<DashboardSummary>>> = observeDashboardReloads()
        .loadRefreshableList(viewModelScope, refreshes) { getDashboards().map { it.sortedForDisplay() } }

    fun refresh() {
        refreshes.tryEmit(Unit)
    }
}

private fun List<DashboardSummary>.sortedForDisplay() = sortedWith(
    compareByDescending<DashboardSummary> { it.pinned }.thenByDescending { it.lastAccessedAt }.thenBy { it.name },
)
