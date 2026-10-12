package com.nightcode.snout.shared.domain.usecase.activitylog

import com.nightcode.snout.shared.domain.data.activitylog.ActivityLogRepository
import com.nightcode.snout.shared.domain.model.activitylog.ActivityEntry
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class ObserveActivityLog(
    private val observeSelectedProjectId: ObserveSelectedProjectId,
    private val repository: ActivityLogRepository,
) {
    operator fun invoke(): Flow<List<ActivityEntry>?> =
        observeSelectedProjectId.flatMapProject { repository.observeActivity(it) }
}
