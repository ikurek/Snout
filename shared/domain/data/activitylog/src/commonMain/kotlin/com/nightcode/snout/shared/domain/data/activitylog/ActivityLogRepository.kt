package com.nightcode.snout.shared.domain.data.activitylog

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.activitylog.ActivityEntry
import com.nightcode.snout.shared.domain.model.common.DataError

interface ActivityLogRepository {
    suspend fun getActivity(projectId: Long): Either<DataError, List<ActivityEntry>>
}
