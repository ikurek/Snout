package com.nightcode.snout.shared.domain.usecase.errortracking

import com.nightcode.snout.shared.domain.data.errortracking.ErrorTrackingRepository
import com.nightcode.snout.shared.domain.model.errortracking.ErrorTrackingIssue
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class ObserveErrorTrackingIssues(
    private val observeSelectedProjectId: ObserveSelectedProjectId,
    private val repository: ErrorTrackingRepository,
) {
    operator fun invoke(status: IssueStatusFilter): Flow<List<ErrorTrackingIssue>?> =
        observeSelectedProjectId.flatMapProject { repository.observeIssues(it, status) }
}
