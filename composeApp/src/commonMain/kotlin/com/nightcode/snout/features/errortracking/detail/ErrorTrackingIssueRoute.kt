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
import snout.composeapp.generated.resources.error_tracking_assign_error_forbidden
import snout.composeapp.generated.resources.error_tracking_assign_error_generic
import snout.composeapp.generated.resources.error_tracking_assigned
import snout.composeapp.generated.resources.error_tracking_assigned_to
import snout.composeapp.generated.resources.error_tracking_members_error_forbidden
import snout.composeapp.generated.resources.error_tracking_members_error_generic
import snout.composeapp.generated.resources.error_tracking_status_changed_active
import snout.composeapp.generated.resources.error_tracking_status_changed_resolved
import snout.composeapp.generated.resources.error_tracking_status_changed_suppressed
import snout.composeapp.generated.resources.error_tracking_status_error_forbidden
import snout.composeapp.generated.resources.error_tracking_status_error_generic
import snout.composeapp.generated.resources.error_tracking_unassigned_message
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
            snackbarHostState.show(
                effect = effect,
                onStatusUndone = viewModel::changeStatus,
                onAssigneeUndone = viewModel::onAssigneeSelected,
            )
        }
    }

    ErrorTrackingIssueScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onRefresh = viewModel::refresh,
        onChangeStatus = viewModel::changeStatus,
        onOpenInPostHog = { openInPostHog(PostHogResource.ErrorTrackingIssue(navKey.issueId)) },
        onOccurrenceSelected = viewModel::onOccurrenceSelected,
        onExceptionsRetried = viewModel::onExceptionsRetried,
        onAssigneeClicked = viewModel::onAssigneeClicked,
        onAssigneeSelected = viewModel::onAssigneeSelected,
        onAssigneePickerDismissed = viewModel::onAssigneePickerDismissed,
    )
}

private suspend fun ErrorTrackingIssueSideEffect.AssigneeChanged.message(): String = when {
    userId == null -> getString(Res.string.error_tracking_unassigned_message)
    assigneeName != null -> getString(Res.string.error_tracking_assigned_to, assigneeName)
    else -> getString(Res.string.error_tracking_assigned)
}

private fun IssueStatusFilter.changedMessage() = when (this) {
    IssueStatusFilter.Active -> Res.string.error_tracking_status_changed_active
    IssueStatusFilter.Resolved -> Res.string.error_tracking_status_changed_resolved
    IssueStatusFilter.Suppressed -> Res.string.error_tracking_status_changed_suppressed
}

private suspend fun SnackbarHostState.show(
    effect: ErrorTrackingIssueSideEffect,
    onStatusUndone: (IssueStatusFilter) -> Unit,
    onAssigneeUndone: (Long?) -> Unit,
) {
    when (effect) {
        is ErrorTrackingIssueSideEffect.StatusChanged -> {
            val result = showSnackbar(
                message = getString(effect.status.changedMessage()),
                actionLabel = effect.undo?.let { getString(Res.string.error_tracking_undo) },
            )
            if (result == SnackbarResult.ActionPerformed) effect.undo?.let(onStatusUndone)
        }

        is ErrorTrackingIssueSideEffect.StatusChangeFailed -> showSnackbar(
            getString(
                effect.error.actionFailedMessage(
                    generic = Res.string.error_tracking_status_error_generic,
                    forbidden = Res.string.error_tracking_status_error_forbidden,
                ),
            ),
        )

        is ErrorTrackingIssueSideEffect.AssigneeChanged -> {
            val result = showSnackbar(
                message = effect.message(),
                actionLabel = effect.undo?.let { getString(Res.string.error_tracking_undo) },
            )
            if (result == SnackbarResult.ActionPerformed) {
                effect.undo?.let { onAssigneeUndone(it.userId) }
            }
        }

        is ErrorTrackingIssueSideEffect.AssignFailed -> showSnackbar(
            getString(
                effect.error.actionFailedMessage(
                    generic = Res.string.error_tracking_assign_error_generic,
                    forbidden = Res.string.error_tracking_assign_error_forbidden,
                ),
            ),
        )

        is ErrorTrackingIssueSideEffect.MembersUnavailable -> showSnackbar(
            getString(
                effect.error.actionFailedMessage(
                    generic = Res.string.error_tracking_members_error_generic,
                    forbidden = Res.string.error_tracking_members_error_forbidden,
                ),
            ),
        )
    }
}
