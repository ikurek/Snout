package com.nightcode.snout.features.experiments

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.nightcode.snout.common.compose.rememberOpenInPostHog
import com.nightcode.snout.shared.domain.model.appstate.PostHogResource
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
internal data object ExperimentsNavKey : NavKey

@Composable
internal fun ExperimentsRoute(onNavigateBack: () -> Unit, onOpenExperiment: (experimentId: Long) -> Unit) {
    val viewModel = koinViewModel<ExperimentsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ExperimentsScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onRefresh = viewModel::refresh,
        onOpenExperiment = onOpenExperiment,
        onOpenInPostHog = rememberOpenInPostHog(PostHogResource.Experiments),
    )
}
