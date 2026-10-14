package com.nightcode.snout.shared.data.errortracking

import arrow.core.Either
import com.nightcode.snout.shared.api.errortracking.ErrorTrackingApi
import com.nightcode.snout.shared.data.common.ChangeSignal
import com.nightcode.snout.shared.data.errortracking.mapper.apiValue
import com.nightcode.snout.shared.data.errortracking.mapper.toAppVersions
import com.nightcode.snout.shared.data.errortracking.mapper.toIssue
import com.nightcode.snout.shared.data.errortracking.mapper.toIssueStatus
import com.nightcode.snout.shared.domain.data.errortracking.ErrorTrackingRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.errortracking.AppVersion
import com.nightcode.snout.shared.domain.model.errortracking.ErrorTrackingIssue
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatus
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single(binds = [ErrorTrackingRepository::class])
internal class ErrorTrackingRepositoryImpl(private val api: ErrorTrackingApi) : ErrorTrackingRepository {

    private val changeSignal = ChangeSignal()
    override val changes: Flow<Unit> = changeSignal.changes

    override suspend fun getIssues(
        projectId: Long,
        status: IssueStatusFilter,
    ): Either<DataError, List<ErrorTrackingIssue>> = api.getIssues(projectId, status.apiValue)
        .map { response -> response.results.map { it.toIssue() } }

    override suspend fun searchIssues(
        projectId: Long,
        status: IssueStatusFilter,
        query: String,
        appVersion: String?,
    ): Either<DataError, List<ErrorTrackingIssue>> =
        api.getIssues(projectId, status.apiValue, search = query, appVersion = appVersion)
            .map { response -> response.results.map { it.toIssue() } }

    override suspend fun getIssue(projectId: Long, issueId: String): Either<DataError, ErrorTrackingIssue> =
        api.getIssue(projectId, issueId).map { it.toIssue() }

    override suspend fun setStatus(
        projectId: Long,
        issueId: String,
        status: IssueStatusFilter,
    ): Either<DataError, IssueStatus> = api.setStatus(projectId, issueId, status.apiValue)
        .map { it.status.toIssueStatus() }
        .onRight { changeSignal.emit() }

    override suspend fun getAppVersions(projectId: Long): Either<DataError, List<AppVersion>> =
        api.getAppVersions(projectId).map { it.toAppVersions() }
}
