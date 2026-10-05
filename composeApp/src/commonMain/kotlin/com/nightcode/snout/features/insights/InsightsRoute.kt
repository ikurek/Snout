package com.nightcode.snout.features.insights

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.insights_favorite_failed

@Composable
internal fun InsightsRoute(onOpenInsight: (insightId: Long) -> Unit) {
    val viewModel = koinViewModel<InsightsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.sideEffects) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                InsightsSideEffect.FavoriteUpdateFailed ->
                    snackbarHostState.showSnackbar(getString(Res.string.insights_favorite_failed))
            }
        }
    }

    InsightsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onQueryChange = viewModel::onQueryChange,
        onRefresh = viewModel::refresh,
        onFavoriteToggle = viewModel::onFavoriteToggle,
        onOpenInsight = onOpenInsight,
    )
}
