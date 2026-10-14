package com.nightcode.snout.features.insights

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcode.snout.common.compose.RefreshOnEnter
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.insights_favorite_failed

@Composable
internal fun InsightsRoute(onOpenInsight: (insightId: Long) -> Unit, contentPadding: PaddingValues = PaddingValues()) {
    val viewModel = koinViewModel<InsightsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnEnter(viewModel::onScreenEntered)
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
        onQueryChanged = viewModel::onQueryChanged,
        onRefresh = viewModel::refresh,
        onToggleFavorite = viewModel::toggleFavorite,
        onOpenInsight = onOpenInsight,
        contentPadding = contentPadding,
    )
}
