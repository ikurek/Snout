package com.nightcode.snout.features.errortracking.detail

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
internal data class ErrorTrackingIssueNavKey(val issueId: String) : NavKey

@Composable
internal fun ErrorTrackingIssueRoute(navKey: ErrorTrackingIssueNavKey, onNavigateBack: () -> Unit) {
    val viewModel = koinViewModel<ErrorTrackingIssueViewModel> { parametersOf(navKey.issueId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ErrorTrackingIssueScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onRetry = viewModel::load,
        onOpenInPostHog = rememberOpenInPostHog(PostHogResource.ErrorTrackingIssue(navKey.issueId)),
    )
}
