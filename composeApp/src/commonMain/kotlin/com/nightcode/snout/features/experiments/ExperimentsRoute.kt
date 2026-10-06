package com.nightcode.snout.features.experiments

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ExperimentsRoute(onOpenExperiment: (experimentId: Long) -> Unit) {
    val viewModel = koinViewModel<ExperimentsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ExperimentsScreen(
        state = state,
        onRefresh = viewModel::refresh,
        onOpenExperiment = onOpenExperiment,
    )
}
