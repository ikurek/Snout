package com.nightcode.snout.features.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
internal data object SettingsNavKey : NavKey

@Composable
internal fun SettingsRoute(onNavigateBack: () -> Unit) {
    val viewModel = koinViewModel<SettingsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    SettingsScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onRetryAccount = viewModel::loadAccount,
        onOpenInstance = { url -> uriHandler.openUri(url) },
        onThemeModeSelected = viewModel::onThemeModeSelected,
        onLogoutConfirmed = viewModel::onLogoutConfirmed,
    )
}
