package com.nightcode.snout.features.settings.privacy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun PrivacySettingsRoute(onNavigateBack: () -> Unit) {
    val viewModel = koinViewModel<PrivacySettingsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PrivacySettingsScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onUsageToggled = viewModel::onUsageToggled,
        onErrorsToggled = viewModel::onErrorsToggled,
        onPrivacyPolicyClicked = viewModel::onPrivacyPolicyClicked,
    )
}
