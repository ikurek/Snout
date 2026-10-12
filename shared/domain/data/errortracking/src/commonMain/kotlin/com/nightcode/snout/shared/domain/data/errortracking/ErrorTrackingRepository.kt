package com.nightcode.snout.shared.domain.data.errortracking

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.errortracking.AppVersion
import com.nightcode.snout.shared.domain.model.errortracking.ErrorTrackingIssue
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatus
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter
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

    /** Changes the issue's status, which moves it to another status filter's list. */
    suspend fun setStatus(projectId: Long, issueId: String, status: IssueStatusFilter): Either<DataError, IssueStatus>
    suspend fun getAppVersions(projectId: Long): Either<DataError, List<AppVersion>>
}
