package com.nightcode.snout.shared.domain.usecase.errortracking

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.errortracking.ErrorTrackingRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import org.koin.core.annotation.Single

/** PostHog's emails link an issue by an exception fingerprint; screens need the issue id. */
@Single
class FindIssueIdByFingerprint(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: ErrorTrackingRepository,
) {
    suspend operator fun invoke(fingerprint: String): Either<DataError, String?> =
        repository.findIssueIdByFingerprint(getSelectedProjectId(), fingerprint)
}
