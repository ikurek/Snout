package com.nightcode.snout.features.errortracking.detail

import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.errortracking.ErrorTrackingIssue
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter

internal data class ErrorTrackingIssueUiState(
    /** The issue; null until the first load succeeds. */
    val issue: ErrorTrackingIssue? = null,
    /** Why the last load failed; a banner over [issue] once it's loaded. */
    val error: DataError? = null,
    val isRefreshing: Boolean = false,
    /** Status being applied right now; the matching button shows progress and the others wait. */
    val updatingTo: IssueStatusFilter? = null,
)

internal sealed interface ErrorTrackingIssueSideEffect {
    /** [undo] is the status to go back to, or null when the previous one can't be set again from the app. */
    data class StatusChanged(val status: IssueStatusFilter, val undo: IssueStatusFilter?) :
        ErrorTrackingIssueSideEffect

    data class StatusChangeFailed(val error: DataError) : ErrorTrackingIssueSideEffect
}
