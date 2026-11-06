package com.nightcode.snout.shared.domain.usecase.errortracking

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.errortracking.ErrorTrackingRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.errortracking.ExceptionOccurrence
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import org.koin.core.annotation.Single

@Single
class GetIssueOccurrences(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: ErrorTrackingRepository,
) {
    suspend operator fun invoke(issueId: String): Either<DataError, List<ExceptionOccurrence>> =
        repository.getOccurrences(getSelectedProjectId(), issueId)
}
