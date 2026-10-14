package com.nightcode.snout.features.experiments

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcode.snout.common.compose.RefreshOnEnter
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ExperimentsRoute(
    onOpenExperiment: (experimentId: Long) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val viewModel = koinViewModel<ExperimentsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnEnter(viewModel::onScreenEntered)

    ExperimentsScreen(
        state = state,
        onRefresh = viewModel::refresh,
        onOpenExperiment = onOpenExperiment,
        contentPadding = contentPadding,
    )
}
