package com.nightcode.snout.features.featureflags.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.nightcode.snout.common.compose.rememberOpenInPostHog
import com.nightcode.snout.shared.domain.model.links.PostHogResource
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
internal data class FeatureFlagDetailNavKey(val flagId: Long) : NavKey

@Composable
internal fun FeatureFlagDetailRoute(navKey: FeatureFlagDetailNavKey, onNavigateBack: () -> Unit) {
    val viewModel = koinViewModel<FeatureFlagDetailViewModel> { parametersOf(navKey.flagId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    FeatureFlagDetailScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onRetry = viewModel::load,
        onToggleConfirmed = viewModel::onToggleConfirmed,
        onDismissToggleError = viewModel::dismissToggleError,
        onOpenInPostHog = rememberOpenInPostHog(PostHogResource.FeatureFlag(navKey.flagId)),
    )
}
