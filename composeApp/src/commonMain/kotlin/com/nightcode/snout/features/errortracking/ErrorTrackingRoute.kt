package com.nightcode.snout.features.errortracking

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ErrorTrackingRoute(
    onOpenIssue: (issueId: String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val viewModel = koinViewModel<ErrorTrackingViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ErrorTrackingScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onStatusChange = viewModel::onStatusChange,
        onAppVersionChange = viewModel::onAppVersionChange,
        onRefresh = viewModel::refresh,
        onOpenIssue = onOpenIssue,
        contentPadding = contentPadding,
    )
}
