package com.nightcode.snout.features.activitylog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.nightcode.snout.common.compose.rememberOpenInPostHog
import com.nightcode.snout.shared.domain.model.appstate.PostHogResource
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
internal data object ActivityLogNavKey : NavKey

@Composable
internal fun ActivityLogRoute(onNavigateBack: () -> Unit) {
    val viewModel = koinViewModel<ActivityLogViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ActivityLogScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onRefresh = viewModel::refresh,
        onOpenInPostHog = rememberOpenInPostHog(PostHogResource.ActivityLog),
    )
}
