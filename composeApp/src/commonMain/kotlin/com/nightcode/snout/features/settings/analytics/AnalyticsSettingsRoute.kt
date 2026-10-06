package com.nightcode.snout.features.settings.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
internal data object AnalyticsSettingsNavKey : NavKey

@Composable
internal fun AnalyticsSettingsRoute(onNavigateBack: () -> Unit) {
    val viewModel = koinViewModel<AnalyticsSettingsViewModel>()
    val preferences by viewModel.uiState.collectAsStateWithLifecycle()

    AnalyticsSettingsScreen(
        preferences = preferences,
        onNavigateBack = onNavigateBack,
        onDefaultRangeSelected = viewModel::onDefaultRangeSelected,
        onMetricToggled = viewModel::onMetricToggled,
        onVisitorsTrendToggled = viewModel::onVisitorsTrendToggled,
        onBreakdownToggled = viewModel::onBreakdownToggled,
        onReset = viewModel::onReset,
    )
}
