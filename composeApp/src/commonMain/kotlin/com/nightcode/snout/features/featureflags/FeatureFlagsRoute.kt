package com.nightcode.snout.features.featureflags

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun FeatureFlagsRoute(onOpenFlag: (flagId: Long) -> Unit) {
    val viewModel = koinViewModel<FeatureFlagsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    FeatureFlagsScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onRefresh = viewModel::refresh,
        onOpenFlag = onOpenFlag,
    )
}
