package com.nightcode.snout.features.settings.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun NavigationSettingsRoute(onNavigateBack: () -> Unit) {
    val viewModel = koinViewModel<NavigationSettingsViewModel>()
    val preferences by viewModel.uiState.collectAsStateWithLifecycle()

    NavigationSettingsScreen(
        preferences = preferences,
        onNavigateBack = onNavigateBack,
        onSlotSelected = viewModel::onSlotSelected,
        onLandingSelected = viewModel::onLandingSelected,
        onReset = viewModel::reset,
    )
}
