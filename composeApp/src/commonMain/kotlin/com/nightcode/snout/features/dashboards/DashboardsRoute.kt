package com.nightcode.snout.features.dashboards

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun DashboardsRoute(onOpenDashboard: (dashboardId: Long) -> Unit) {
    val viewModel = koinViewModel<DashboardsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardsScreen(state = state, onRefresh = viewModel::refresh, onOpenDashboard = onOpenDashboard)
}
