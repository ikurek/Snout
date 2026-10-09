package com.nightcode.snout.shared.data.errortracking

import arrow.core.Either
import com.nightcode.snout.shared.api.errortracking.ErrorTrackingApi
import com.nightcode.snout.shared.data.common.JsonCacheStore
import com.nightcode.snout.shared.data.common.toDataError
import com.nightcode.snout.shared.data.errortracking.mapper.apiValue
import com.nightcode.snout.shared.data.errortracking.mapper.toAppVersions
import com.nightcode.snout.shared.data.errortracking.mapper.toIssue
import com.nightcode.snout.shared.domain.data.errortracking.ErrorTrackingRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.errortracking.AppVersion
import com.nightcode.snout.shared.domain.model.errortracking.ErrorTrackingIssue
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter
import kotlinx.coroutines.flow.Flow

internal class ErrorTrackingRepositoryImpl(private val api: ErrorTrackingApi, private val cache: JsonCacheStore) :
    ErrorTrackingRepository {

    override fun observeIssues(projectId: Long, status: IssueStatusFilter): Flow<List<ErrorTrackingIssue>?> =
        cache.observe(key(projectId, status))

    override suspend fun refreshIssues(projectId: Long, status: IssueStatusFilter): Either<DataError, Unit> =
        api.getIssues(projectId, status.apiValue)
            .mapLeft { it.toDataError() }
            .map { response -> cache.put(key(projectId, status), response.results.map { it.toIssue() }) }

    override suspend fun searchIssues(
        projectId: Long,
        status: IssueStatusFilter,
        query: String,
        appVersion: String?,
    ): Either<DataError, List<ErrorTrackingIssue>> =
        api.getIssues(projectId, status.apiValue, search = query, appVersion = appVersion)
            .mapLeft { it.toDataError() }
            .map { response -> response.results.map { it.toIssue() } }

    override suspend fun getIssue(projectId: Long, issueId: String): Either<DataError, ErrorTrackingIssue> =
        api.getIssue(projectId, issueId).mapLeft { it.toDataError() }.map { it.toIssue() }

    override suspend fun getAppVersions(projectId: Long): Either<DataError, List<AppVersion>> =
        api.getAppVersions(projectId).mapLeft { it.toDataError() }.map { it.toAppVersions() }

    private fun key(projectId: Long, status: IssueStatusFilter) = "error_tracking_issues:$projectId:${status.apiValue}"
}
