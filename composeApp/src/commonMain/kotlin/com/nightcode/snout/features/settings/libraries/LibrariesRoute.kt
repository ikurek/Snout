package com.nightcode.snout.features.settings.libraries

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun LibrariesRoute(onNavigateBack: () -> Unit, onOpenLibrary: (uniqueId: String) -> Unit) {
    val viewModel = koinViewModel<LibrariesViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LibrariesScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onRefresh = viewModel::refresh,
        onOpenLibrary = onOpenLibrary,
    )
}
