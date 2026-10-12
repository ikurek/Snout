package com.nightcode.snout.features.experiments.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcode.snout.common.compose.rememberPostHogOpener
import com.nightcode.snout.navigation.ExperimentDetailNavKey
import com.nightcode.snout.shared.domain.model.links.PostHogResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun ExperimentDetailRoute(
    navKey: ExperimentDetailNavKey,
    onNavigateBack: () -> Unit,
    onOpenFeatureFlag: (flagId: Long) -> Unit,
) {
    val viewModel = koinViewModel<ExperimentDetailViewModel> { parametersOf(navKey.experimentId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val openInPostHog = rememberPostHogOpener()

    ExperimentDetailScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onRefresh = viewModel::refresh,
        onOpenFeatureFlag = onOpenFeatureFlag,
        onOpenInPostHog = { openInPostHog(PostHogResource.Experiment(navKey.experimentId)) },
    )
}
