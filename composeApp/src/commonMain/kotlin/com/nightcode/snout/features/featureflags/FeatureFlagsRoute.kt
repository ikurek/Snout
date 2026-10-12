package com.nightcode.snout.features.featureflags

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun FeatureFlagsRoute(onOpenFlag: (flagId: Long) -> Unit, contentPadding: PaddingValues = PaddingValues()) {
    val viewModel = koinViewModel<FeatureFlagsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    FeatureFlagsScreen(
        state = state,
        onQueryChanged = viewModel::onQueryChanged,
        onRefresh = viewModel::refresh,
        onOpenFlag = onOpenFlag,
        contentPadding = contentPadding,
    )
}
