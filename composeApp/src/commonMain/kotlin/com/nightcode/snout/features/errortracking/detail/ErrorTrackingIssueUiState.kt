package com.nightcode.snout.features.errortracking.detail

import com.nightcode.snout.common.state.LoadState
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.errortracking.ErrorTrackingIssue
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter

internal data class ErrorTrackingIssueUiState(
    val issue: LoadState<ErrorTrackingIssue> = LoadState.Loading,
    /** Status being applied right now; the matching button shows progress and the others wait. */
    val updatingTo: IssueStatusFilter? = null,
)

internal sealed interface ErrorTrackingIssueSideEffect {
    /** [undo] is the status to go back to, or null when the previous one can't be set again from the app. */
    data class StatusChanged(val status: IssueStatusFilter, val undo: IssueStatusFilter?) :
        ErrorTrackingIssueSideEffect

    data class StatusChangeFailed(val error: DataError) : ErrorTrackingIssueSideEffect
}
