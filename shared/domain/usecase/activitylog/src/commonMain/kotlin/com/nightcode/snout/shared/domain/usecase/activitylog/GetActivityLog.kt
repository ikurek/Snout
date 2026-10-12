package com.nightcode.snout.shared.domain.usecase.activitylog

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.activitylog.ActivityLogRepository
import com.nightcode.snout.shared.domain.model.activitylog.ActivityEntry
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import org.koin.core.annotation.Single

@Single
class GetActivityLog(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: ActivityLogRepository,
) {
    suspend operator fun invoke(): Either<DataError, List<ActivityEntry>> =
        repository.getActivity(getSelectedProjectId())
}
