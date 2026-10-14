package com.nightcode.snout.features.errortracking.detail

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcode.snout.common.compose.RefreshOnEnter
import com.nightcode.snout.common.compose.rememberPostHogOpener
import com.nightcode.snout.designsystem.actionFailedMessage
import com.nightcode.snout.navigation.ErrorTrackingIssueNavKey
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter
import com.nightcode.snout.shared.domain.model.links.PostHogResource
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.error_tracking_status_changed_active
import snout.composeapp.generated.resources.error_tracking_status_changed_resolved
import snout.composeapp.generated.resources.error_tracking_status_changed_suppressed
import snout.composeapp.generated.resources.error_tracking_status_error_forbidden
import snout.composeapp.generated.resources.error_tracking_status_error_generic
import snout.composeapp.generated.resources.error_tracking_undo

@Composable
internal fun ErrorTrackingIssueRoute(navKey: ErrorTrackingIssueNavKey, onNavigateBack: () -> Unit) {
    val viewModel = koinViewModel<ErrorTrackingIssueViewModel> { parametersOf(navKey.issueId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnEnter(viewModel::onScreenEntered)
    val openInPostHog = rememberPostHogOpener()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.sideEffects) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is ErrorTrackingIssueSideEffect.StatusChanged -> {
                    val result = snackbarHostState.showSnackbar(
                        message = getString(effect.status.changedMessage()),
                        actionLabel = effect.undo?.let { getString(Res.string.error_tracking_undo) },
                    )
                    if (result == SnackbarResult.ActionPerformed) effect.undo?.let(viewModel::changeStatus)
                }

                is ErrorTrackingIssueSideEffect.StatusChangeFailed -> snackbarHostState.showSnackbar(
                    getString(
                        effect.error.actionFailedMessage(
                            generic = Res.string.error_tracking_status_error_generic,
                            forbidden = Res.string.error_tracking_status_error_forbidden,
                        ),
                    ),
                )
            }
        }
    }

    ErrorTrackingIssueScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onRefresh = viewModel::refresh,
        onChangeStatus = viewModel::changeStatus,
        onOpenInPostHog = { openInPostHog(PostHogResource.ErrorTrackingIssue(navKey.issueId)) },
    )
}

private fun IssueStatusFilter.changedMessage() = when (this) {
    IssueStatusFilter.Active -> Res.string.error_tracking_status_changed_active
    IssueStatusFilter.Resolved -> Res.string.error_tracking_status_changed_resolved
    IssueStatusFilter.Suppressed -> Res.string.error_tracking_status_changed_suppressed
}
