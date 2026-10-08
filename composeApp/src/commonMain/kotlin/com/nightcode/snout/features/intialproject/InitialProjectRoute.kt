package com.nightcode.snout.features.intialproject

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
internal data object InitialProjectNavKey : NavKey

@Composable
internal fun InitialProjectRoute(onProjectSelected: () -> Unit) {
    val viewModel = koinViewModel<InitialProjectViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { sideEffect ->
            when (sideEffect) {
                InitialProjectSideEffect.ProjectSelected -> onProjectSelected()
            }
        }
    }

    InitialProjectScreen(
        state = state,
        onRetry = viewModel::load,
        onProjectSelected = viewModel::onProjectSelected,
    )
}
