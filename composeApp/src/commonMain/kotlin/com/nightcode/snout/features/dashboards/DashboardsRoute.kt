package com.nightcode.snout.features.dashboards

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcode.snout.common.compose.RefreshOnEnter
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun DashboardsRoute(
    onOpenDashboard: (dashboardId: Long) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val viewModel = koinViewModel<DashboardsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnEnter(viewModel::onScreenEntered)

    DashboardsScreen(
        state = state,
        onRefresh = viewModel::refresh,
        onOpenDashboard = onOpenDashboard,
        contentPadding = contentPadding,
    )
}
