package com.nightcode.snout.features.activitylog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ActivityLogRoute() {
    val viewModel = koinViewModel<ActivityLogViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ActivityLogScreen(state = state, onRefresh = viewModel::refresh)
}
