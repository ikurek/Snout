package com.nightcode.snout.features.featureflags.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcode.snout.common.compose.rememberPostHogOpener
import com.nightcode.snout.navigation.FeatureFlagDetailNavKey
import com.nightcode.snout.shared.domain.model.links.PostHogResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun FeatureFlagDetailRoute(navKey: FeatureFlagDetailNavKey, onNavigateBack: () -> Unit) {
    val viewModel = koinViewModel<FeatureFlagDetailViewModel> { parametersOf(navKey.flagId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val openInPostHog = rememberPostHogOpener()

    FeatureFlagDetailScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onRefresh = viewModel::refresh,
        onToggleConfirmed = viewModel::onToggleConfirmed,
        onToggleErrorDismissed = viewModel::onToggleErrorDismissed,
        onOpenInPostHog = { openInPostHog(PostHogResource.FeatureFlag(navKey.flagId)) },
    )
}
