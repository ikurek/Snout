package com.nightcode.snout.features.settings.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
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
        onReset = viewModel::onReset,
    )
}

@Serializable
internal data object NavigationSettingsNavKey : NavKey
