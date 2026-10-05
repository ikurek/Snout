package com.nightcode.snout.shared.domain.data.activitylog

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.activitylog.ActivityEntry
import com.nightcode.snout.shared.domain.model.common.DataError
import kotlinx.coroutines.flow.Flow

interface ActivityLogRepository {
    fun observeActivity(projectId: Long): Flow<List<ActivityEntry>?>
    suspend fun refreshActivity(projectId: Long): Either<DataError, Unit>
}
