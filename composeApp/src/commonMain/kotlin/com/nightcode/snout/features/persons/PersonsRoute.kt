package com.nightcode.snout.features.persons

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcode.snout.common.compose.RefreshOnEnter
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun PersonsRoute(onOpenPerson: (uuid: String) -> Unit, contentPadding: PaddingValues = PaddingValues()) {
    val viewModel = koinViewModel<PersonsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnEnter(viewModel::onScreenEntered)

    PersonsScreen(
        state = state,
        onQueryChanged = viewModel::onQueryChanged,
        onRefresh = viewModel::refresh,
        onOpenPerson = onOpenPerson,
        contentPadding = contentPadding,
    )
}
