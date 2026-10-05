package com.nightcode.snout.features.annotations

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.nightcode.snout.common.compose.rememberOpenInPostHog
import com.nightcode.snout.shared.domain.model.appstate.PostHogResource
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.annotations_created_snackbar

@Serializable
internal data object AnnotationsNavKey : NavKey

@Composable
internal fun AnnotationsRoute(onNavigateBack: () -> Unit) {
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
        onNavigateBack = onNavigateBack,
        onRefresh = viewModel::refresh,
        onAddClick = { showCreateSheet = true },
        onCreateDismiss = {
            showCreateSheet = false
            viewModel.dismissCreateError()
        },
        onCreate = viewModel::onCreate,
        onOpenInPostHog = rememberOpenInPostHog(PostHogResource.Annotations),
    )
}
