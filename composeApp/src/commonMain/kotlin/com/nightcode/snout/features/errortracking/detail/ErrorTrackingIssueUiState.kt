package com.nightcode.snout.features.errortracking.detail

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.errortracking.ErrorTrackingIssue
import com.nightcode.snout.shared.domain.model.errortracking.ExceptionOccurrence
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter
import com.nightcode.snout.shared.domain.model.errortracking.OccurrenceException
import com.nightcode.snout.shared.domain.model.errortracking.TestErrorTrackingIssue
import com.nightcode.snout.shared.domain.model.errortracking.TestExceptionOccurrences
import com.nightcode.snout.shared.domain.model.errortracking.TestOccurrenceExceptions
import com.nightcode.snout.shared.domain.model.organization.OrganizationMember
import com.nightcode.snout.shared.domain.model.organization.TestOrganizationMembers

internal data class ErrorTrackingIssueUiState(
    /** The issue; null until the first load succeeds. */
    val issue: ErrorTrackingIssue? = null,
    /** Why the last load failed; a banner over [issue] once it's loaded. */
    val error: DataError? = null,
    val isRefreshing: Boolean = false,
    /** Status being applied right now; the matching button shows progress and the others wait. */
    val updatingTo: IssueStatusFilter? = null,
    /** The issue's latest occurrences, newest first; null until they load. */
    val occurrences: List<ExceptionOccurrence>? = null,
    val occurrencesError: DataError? = null,
    /** The occurrence whose exceptions are shown; the latest one unless another was picked. */
    val selectedOccurrenceId: String? = null,
    /** The selected occurrence's exceptions; null while they load. */
    val exceptions: List<OccurrenceException>? = null,
    val exceptionsError: DataError? = null,
    /** Null until they load. */
    val members: List<OrganizationMember>? = null,
    val membersError: DataError? = null,
    /** The assignee picker is asked for; it shows once [members] are loaded. */
    val isAssigneePickerRequested: Boolean = false,
    val isAssigning: Boolean = false,
) {
    val selectedOccurrence: ExceptionOccurrence?
        get() = occurrences?.firstOrNull { it.id == selectedOccurrenceId }

    val isAssigneePickerVisible: Boolean
        get() = isAssigneePickerRequested && members != null
}

internal sealed interface ErrorTrackingIssueSideEffect {
    /** [undo] is the status to go back to, or null when the previous one can't be set again from the app. */
    data class StatusChanged(val status: IssueStatusFilter, val undo: IssueStatusFilter?) :
        ErrorTrackingIssueSideEffect

    data class StatusChangeFailed(val error: DataError) : ErrorTrackingIssueSideEffect

    /**
     * [userId] is who it is assigned to now, null once unassigned, and [assigneeName] their name when the members are
     * loaded. [undo] puts the previous assignee back, and is null when that was a role, which the app can't assign.
     */
    data class AssigneeChanged(val userId: Long?, val assigneeName: String?, val undo: AssigneeUndo?) :
        ErrorTrackingIssueSideEffect

    data class AssignFailed(val error: DataError) : ErrorTrackingIssueSideEffect

    /** The picker couldn't open: the members didn't load. They are being loaded again. */
    data class MembersUnavailable(val error: DataError) : ErrorTrackingIssueSideEffect
}

/** The member to assign again to undo a change, or null for unassigning. */
internal data class AssigneeUndo(val userId: Long?)

@TestData
@OptIn(TestData::class)
internal val TestErrorTrackingIssueUiState = ErrorTrackingIssueUiState(
    issue = TestErrorTrackingIssue,
    occurrences = TestExceptionOccurrences,
    selectedOccurrenceId = TestExceptionOccurrences.first().id,
    exceptions = TestOccurrenceExceptions,
    members = TestOrganizationMembers,
)
