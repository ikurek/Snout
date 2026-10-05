package com.nightcode.snout.shared.data.activitylog

import arrow.core.Either
import com.nightcode.snout.shared.api.activitylog.ActivityLogApi
import com.nightcode.snout.shared.data.activitylog.mapper.toEntry
import com.nightcode.snout.shared.data.common.JsonCacheStore
import com.nightcode.snout.shared.data.common.toDataError
import com.nightcode.snout.shared.domain.data.activitylog.ActivityLogRepository
import com.nightcode.snout.shared.domain.model.activitylog.ActivityEntry
import com.nightcode.snout.shared.domain.model.common.DataError
import kotlinx.coroutines.flow.Flow

internal class ActivityLogRepositoryImpl(private val api: ActivityLogApi, private val cache: JsonCacheStore) :
    ActivityLogRepository {

    override fun observeActivity(projectId: Long): Flow<List<ActivityEntry>?> = cache.observe(key(projectId))

    override suspend fun refreshActivity(projectId: Long): Either<DataError, Unit> = api.getActivityLog(projectId)
        .mapLeft { it.toDataError() }
        .map { response ->
            cache.put(
                key(projectId),
                response.results.map { it.toEntry() }.sortedByDescending { it.createdAt },
            )
        }

    private fun key(projectId: Long) = "activity_log:$projectId"
}
