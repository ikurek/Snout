package com.nightcode.snout.features.experiments.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.nightcode.snout.common.compose.rememberOpenInPostHog
import com.nightcode.snout.shared.domain.model.appstate.PostHogResource
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
internal data class ExperimentDetailNavKey(val experimentId: Long) : NavKey

@Composable
internal fun ExperimentDetailRoute(
    navKey: ExperimentDetailNavKey,
    onNavigateBack: () -> Unit,
    onOpenFeatureFlag: (flagId: Long) -> Unit,
) {
    val viewModel = koinViewModel<ExperimentDetailViewModel> { parametersOf(navKey.experimentId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ExperimentDetailScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onRetry = viewModel::load,
        onOpenFeatureFlag = onOpenFeatureFlag,
        onOpenInPostHog = rememberOpenInPostHog(PostHogResource.Experiment(navKey.experimentId)),
    )
}
