package com.nightcode.snout.features.activitylog

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcode.snout.common.compose.RefreshOnEnter
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ActivityLogRoute(contentPadding: PaddingValues = PaddingValues()) {
    val viewModel = koinViewModel<ActivityLogViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnEnter(viewModel::onScreenEntered)

    ActivityLogScreen(state = state, onRefresh = viewModel::refresh, contentPadding = contentPadding)
}
