package com.nightcode.snout.shared.domain.usecase.errortracking

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.errortracking.ErrorTrackingRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.errortracking.AppVersion
import com.nightcode.snout.shared.domain.model.errortracking.ErrorTrackingIssue
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveErrorTrackingIssues(
    private val observeSelectedProjectId: ObserveSelectedProjectId,
    private val repository: ErrorTrackingRepository,
) {
    operator fun invoke(status: IssueStatusFilter): Flow<List<ErrorTrackingIssue>?> =
        observeSelectedProjectId().flatMapLatest { repository.observeIssues(it, status) }
}

class RefreshErrorTrackingIssues(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: ErrorTrackingRepository,
) {
    suspend operator fun invoke(status: IssueStatusFilter): Either<DataError, Unit> =
        repository.refreshIssues(getSelectedProjectId(), status)
}

class SearchErrorTrackingIssues(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: ErrorTrackingRepository,
) {
    suspend operator fun invoke(
        status: IssueStatusFilter,
        query: String,
        appVersion: String?,
    ): Either<DataError, List<ErrorTrackingIssue>> =
        repository.searchIssues(getSelectedProjectId(), status, query, appVersion)
}

class GetErrorTrackingIssue(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: ErrorTrackingRepository,
) {
    suspend operator fun invoke(issueId: String): Either<DataError, ErrorTrackingIssue> =
        repository.getIssue(getSelectedProjectId(), issueId)
}

class GetErrorAppVersions(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: ErrorTrackingRepository,
) {
    suspend operator fun invoke(): Either<DataError, List<AppVersion>> =
        repository.getAppVersions(getSelectedProjectId())
}
