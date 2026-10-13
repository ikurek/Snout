package com.nightcode.snout.features.settings.libraries.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcode.snout.navigation.LibraryDetailNavKey
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun LibraryDetailRoute(navKey: LibraryDetailNavKey, onNavigateBack: () -> Unit) {
    val viewModel = koinViewModel<LibraryDetailViewModel> { parametersOf(navKey) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LibraryDetailScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onRefresh = viewModel::refresh,
        onOpenWebsite = viewModel::openWebsite,
    )
}
