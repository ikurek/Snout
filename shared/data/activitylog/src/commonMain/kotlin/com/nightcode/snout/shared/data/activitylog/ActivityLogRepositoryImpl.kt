package com.nightcode.snout.shared.data.activitylog

import arrow.core.Either
import com.nightcode.snout.shared.api.activitylog.ActivityLogApi
import com.nightcode.snout.shared.data.activitylog.mapper.toEntry
import com.nightcode.snout.shared.domain.data.activitylog.ActivityLogRepository
import com.nightcode.snout.shared.domain.model.activitylog.ActivityEntry
import com.nightcode.snout.shared.domain.model.common.DataError
import org.koin.core.annotation.Single

@Single(binds = [ActivityLogRepository::class])
internal class ActivityLogRepositoryImpl(private val api: ActivityLogApi) : ActivityLogRepository {

    override suspend fun getActivity(projectId: Long): Either<DataError, List<ActivityEntry>> =
        api.getActivityLog(projectId)
            .map { response -> response.results.map { it.toEntry() }.sortedByDescending { it.createdAt } }
}
