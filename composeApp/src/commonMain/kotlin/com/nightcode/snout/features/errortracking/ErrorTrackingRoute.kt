package com.nightcode.snout.features.errortracking

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcode.snout.common.compose.RefreshOnEnter
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ErrorTrackingRoute(
    onOpenIssue: (issueId: String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val viewModel = koinViewModel<ErrorTrackingViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnEnter(viewModel::onScreenEntered)

    ErrorTrackingScreen(
        state = state,
        onQueryChanged = viewModel::onQueryChanged,
        onStatusChanged = viewModel::onStatusChanged,
        onAppVersionChanged = viewModel::onAppVersionChanged,
        onRefresh = viewModel::refresh,
        onOpenIssue = onOpenIssue,
        contentPadding = contentPadding,
    )
}
