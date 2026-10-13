package com.nightcode.snout.features.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcode.snout.features.projectswitcher.ProjectSwitcherSheet
import com.nightcode.snout.features.projectswitcher.ProjectSwitcherViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SettingsRoute(
    onNavigateBack: () -> Unit,
    onOpenNavigationSettings: () -> Unit,
    onOpenAnalyticsSettings: () -> Unit,
    onOpenLibraries: () -> Unit,
) {
    val viewModel = koinViewModel<SettingsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val switcherViewModel = koinViewModel<ProjectSwitcherViewModel>()
    val switcherState by switcherViewModel.uiState.collectAsStateWithLifecycle()
    var showProjectSwitcher by rememberSaveable { mutableStateOf(false) }

    SettingsScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onRetryAccount = viewModel::refresh,
        onOpenInstance = viewModel::openInstance,
        onSwitchProject = { showProjectSwitcher = true },
        onOpenLinkSettings = viewModel::openLinkSettings,
        onThemeModeSelected = viewModel::onThemeModeSelected,
        onOpenNavigationSettings = onOpenNavigationSettings,
        onOpenAnalyticsSettings = onOpenAnalyticsSettings,
        onOpenLibraries = onOpenLibraries,
        onLogoutConfirmed = viewModel::onLogoutConfirmed,
    )

    ProjectSwitcherSheet(
        isVisible = showProjectSwitcher,
        state = switcherState,
        onProjectSelected = switcherViewModel::onProjectSelected,
        onDismiss = { showProjectSwitcher = false },
    )
}
