package com.nightcode.snout.features.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
internal data object HomeNavKey : NavKey

@Composable
internal fun HomeRoute(onNavigateToSettings: () -> Unit) {
    val viewModel = koinViewModel<ProjectSwitcherViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showProjectSwitcher by rememberSaveable { mutableStateOf(false) }

    HomeScreen(
        currentProjectName = uiState.currentProjectName,
        onOpenProjectSwitcher = { showProjectSwitcher = true },
        onNavigateToSettings = onNavigateToSettings,
    )

    ProjectSwitcherBottomSheet(
        isVisible = showProjectSwitcher,
        state = uiState,
        onProjectSelected = viewModel::onProjectSelected,
        onDismiss = { showProjectSwitcher = false },
    )
}
