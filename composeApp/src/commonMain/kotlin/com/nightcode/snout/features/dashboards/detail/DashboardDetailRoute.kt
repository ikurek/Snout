package com.nightcode.snout.features.dashboards.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.nightcode.snout.common.compose.rememberOpenInPostHog
import com.nightcode.snout.shared.domain.model.links.PostHogResource
import kotlinx.serialization.Serializable
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
    val openInPostHog = rememberOpenInPostHog(PostHogResource.Dashboard(navKey.dashboardId))

    DashboardDetailScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onRefresh = viewModel::refresh,
        onRetryTile = viewModel::retryTile,
        onOpenInsight = onOpenInsight,
        onOpenInPostHog = openInPostHog,
    )
}

@Serializable
internal data class DashboardDetailNavKey(val dashboardId: Long) : NavKey
