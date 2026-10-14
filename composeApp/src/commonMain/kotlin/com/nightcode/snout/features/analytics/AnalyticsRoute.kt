package com.nightcode.snout.features.analytics

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcode.snout.common.compose.RefreshOnEnter
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun AnalyticsRoute(contentPadding: PaddingValues = PaddingValues()) {
    val viewModel = koinViewModel<AnalyticsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnEnter(viewModel::onScreenEntered)

    AnalyticsScreen(
        state = state,
        onRangeSelected = viewModel::onRangeSelected,
        onRefresh = viewModel::refresh,
        contentPadding = contentPadding,
    )
}
