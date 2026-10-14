package com.nightcode.snout.features.dashboards.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcode.snout.common.compose.RefreshOnEnter
import com.nightcode.snout.common.compose.rememberPostHogOpener
import com.nightcode.snout.navigation.DashboardDetailNavKey
import com.nightcode.snout.shared.domain.model.links.PostHogResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun DashboardDetailRoute(
    navKey: DashboardDetailNavKey,
    onNavigateBack: () -> Unit,
    onOpenInsight: (insightId: Long) -> Unit,
) {
    val viewModel = koinViewModel<DashboardDetailViewModel> { parametersOf(navKey.dashboardId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnEnter(viewModel::onScreenEntered)
    val openInPostHog = rememberPostHogOpener()

    DashboardDetailScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onRefresh = viewModel::refresh,
        onRetryTile = viewModel::retryTile,
        onOpenInsight = onOpenInsight,
        onOpenInPostHog = { openInPostHog(PostHogResource.Dashboard(navKey.dashboardId)) },
    )
}
