package com.nightcode.snout.shared.domain.data.errortracking

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.errortracking.AppVersion
import com.nightcode.snout.shared.domain.model.errortracking.ErrorTrackingIssue
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter
import kotlinx.coroutines.flow.Flow

interface ErrorTrackingRepository {
    fun observeIssues(projectId: Long, status: IssueStatusFilter): Flow<List<ErrorTrackingIssue>?>
    suspend fun refreshIssues(projectId: Long, status: IssueStatusFilter): Either<DataError, Unit>

    /** Fetches without caching; a blank [query] or null [appVersion] leaves that filter out. */
    suspend fun searchIssues(
        projectId: Long,
        status: IssueStatusFilter,
        query: String,
        appVersion: String?,
    ): Either<DataError, List<ErrorTrackingIssue>>
    suspend fun getIssue(projectId: Long, issueId: String): Either<DataError, ErrorTrackingIssue>
    suspend fun getAppVersions(projectId: Long): Either<DataError, List<AppVersion>>
}
