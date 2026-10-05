package com.nightcode.snout.features.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.nightcode.snout.features.more.MoreItem
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
internal data object HomeNavKey : NavKey

@Composable
internal fun HomeRoute(
    onNavigateToSettings: () -> Unit,
    onOpenDashboard: (dashboardId: Long) -> Unit,
    onOpenInsight: (insightId: Long) -> Unit,
    onMoreItemClick: (MoreItem) -> Unit,
) {
    val viewModel = koinViewModel<ProjectSwitcherViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showProjectSwitcher by rememberSaveable { mutableStateOf(false) }

    HomeScreen(
        currentProjectName = uiState.currentProjectName,
        onOpenProjectSwitcher = { showProjectSwitcher = true },
        onNavigateToSettings = onNavigateToSettings,
        onOpenDashboard = onOpenDashboard,
        onOpenInsight = onOpenInsight,
        onMoreItemClick = onMoreItemClick,
    )

    ProjectSwitcherBottomSheet(
        isVisible = showProjectSwitcher,
        state = uiState,
        onProjectSelected = viewModel::onProjectSelected,
        onDismiss = { showProjectSwitcher = false },
    )
}
