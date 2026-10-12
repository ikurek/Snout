package com.nightcode.snout.features.annotations

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.annotations_created_snackbar

@Composable
internal fun AnnotationsRoute(contentPadding: PaddingValues = PaddingValues()) {
    val viewModel = koinViewModel<AnnotationsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel.sideEffects) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                AnnotationsSideEffect.Created -> {
                    showCreateSheet = false
                    snackbarHostState.showSnackbar(getString(Res.string.annotations_created_snackbar))
                }
            }
        }
    }

    AnnotationsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        showCreateSheet = showCreateSheet,
        onRefresh = viewModel::refresh,
        onAddClicked = { showCreateSheet = true },
        onCreateDismissed = {
            showCreateSheet = false
            viewModel.onCreateErrorDismissed()
        },
        onCreate = viewModel::create,
        contentPadding = contentPadding,
    )
}
