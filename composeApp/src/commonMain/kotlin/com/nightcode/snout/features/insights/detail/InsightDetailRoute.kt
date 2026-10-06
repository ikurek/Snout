package com.nightcode.snout.features.insights.detail

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.nightcode.snout.common.compose.rememberPostHogOpener
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.shared.domain.model.appstate.PostHogResource
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.insights_favorite_failed

@Serializable
internal data class InsightDetailNavKey(val insightId: Long) : NavKey

@Composable
internal fun InsightDetailRoute(navKey: InsightDetailNavKey, onNavigateBack: () -> Unit) {
    val viewModel = koinViewModel<InsightDetailViewModel> { parametersOf(navKey.insightId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val openInPostHog = rememberPostHogOpener()

    LaunchedEffect(viewModel.sideEffects) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                InsightDetailSideEffect.FavoriteUpdateFailed ->
                    snackbarHostState.showSnackbar(getString(Res.string.insights_favorite_failed))
            }
        }
    }

    val shortId = (state as? CachedUiState.Content)?.data?.summary?.shortId
    InsightDetailScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onRetry = viewModel::load,
        onFavoriteToggle = viewModel::onFavoriteToggle,
        onOpenInPostHog = { shortId?.let { openInPostHog(PostHogResource.Insight(it)) } },
    )
}
