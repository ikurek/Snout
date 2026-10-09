package com.nightcode.snout.features.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.nightcode.snout.features.home.ProjectSwitcherBottomSheet
import com.nightcode.snout.features.home.ProjectSwitcherViewModel
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
internal data object SettingsNavKey : NavKey

@Composable
internal fun SettingsRoute(
    onNavigateBack: () -> Unit,
    onOpenNavigationSettings: () -> Unit,
    onOpenAnalyticsSettings: () -> Unit,
) {
    val viewModel = koinViewModel<SettingsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val switcherViewModel = koinViewModel<ProjectSwitcherViewModel>()
    val switcherState by switcherViewModel.uiState.collectAsStateWithLifecycle()
    var showProjectSwitcher by rememberSaveable { mutableStateOf(false) }

    SettingsScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onRetryAccount = viewModel::loadAccount,
        onOpenInstance = { url -> uriHandler.openUri(url) },
        onSwitchProject = { showProjectSwitcher = true },
        onThemeModeSelected = viewModel::onThemeModeSelected,
        onOpenNavigationSettings = onOpenNavigationSettings,
        onOpenAnalyticsSettings = onOpenAnalyticsSettings,
        onLogoutConfirmed = viewModel::onLogoutConfirmed,
    )

    ProjectSwitcherBottomSheet(
        isVisible = showProjectSwitcher,
        state = switcherState,
        onProjectSelected = switcherViewModel::onProjectSelected,
        onDismiss = { showProjectSwitcher = false },
    )
}
