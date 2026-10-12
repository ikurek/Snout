package com.nightcode.snout.features.settings.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun AnalyticsSettingsRoute(onNavigateBack: () -> Unit) {
    val viewModel = koinViewModel<AnalyticsSettingsViewModel>()
    val preferences by viewModel.uiState.collectAsStateWithLifecycle()

    AnalyticsSettingsScreen(
        preferences = preferences,
        onNavigateBack = onNavigateBack,
        onDefaultRangeSelected = viewModel::onDefaultRangeSelected,
        onMetricToggled = viewModel::onMetricToggled,
        onTrendChartToggled = viewModel::onTrendChartToggled,
        onBreakdownToggled = viewModel::onBreakdownToggled,
        onReset = viewModel::reset,
    )
}
