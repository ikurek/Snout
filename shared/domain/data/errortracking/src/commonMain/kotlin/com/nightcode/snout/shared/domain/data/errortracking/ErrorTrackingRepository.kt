package com.nightcode.snout.shared.domain.data.errortracking

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.errortracking.AppVersion
import com.nightcode.snout.shared.domain.model.errortracking.ErrorTrackingIssue
import com.nightcode.snout.shared.domain.model.errortracking.ExceptionOccurrence
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatus
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter
import com.nightcode.snout.shared.domain.model.errortracking.OccurrenceException
import kotlinx.coroutines.flow.Flow

interface ErrorTrackingRepository {
    /** Emits after each successful change made through this app, so lists showing the old state load again. */
    val changes: Flow<Unit>

    suspend fun getIssues(projectId: Long, status: IssueStatusFilter): Either<DataError, List<ErrorTrackingIssue>>

    /** A blank [query] or null [appVersion] leaves that filter out. */
    suspend fun searchIssues(
        projectId: Long,
        status: IssueStatusFilter,
        query: String,
        appVersion: String?,
    ): Either<DataError, List<ErrorTrackingIssue>>
    suspend fun getIssue(projectId: Long, issueId: String): Either<DataError, ErrorTrackingIssue>

    /** [fingerprint] is percent-encoded, as in a link's path. Null when the project has no such fingerprint. */
    suspend fun findIssueIdByFingerprint(projectId: Long, fingerprint: String): Either<DataError, String?>

    /** Changes the issue's status, which moves it to another status filter's list. */
    suspend fun setStatus(projectId: Long, issueId: String, status: IssueStatusFilter): Either<DataError, IssueStatus>
    suspend fun getAppVersions(projectId: Long): Either<DataError, List<AppVersion>>

    /** The issue's latest occurrences of the last 30 days, newest first. */
    suspend fun getOccurrences(projectId: Long, issueId: String): Either<DataError, List<ExceptionOccurrence>>

    /** The exceptions of [occurrence], outermost first; [DataError.NotFound] once the event is gone. */
    suspend fun getOccurrenceExceptions(
        projectId: Long,
        occurrence: ExceptionOccurrence,
    ): Either<DataError, List<OccurrenceException>>

    suspend fun assign(projectId: Long, issueId: String, userId: Long?): Either<DataError, Unit>
}
