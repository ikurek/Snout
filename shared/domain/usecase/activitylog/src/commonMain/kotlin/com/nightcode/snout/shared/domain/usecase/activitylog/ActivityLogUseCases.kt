package com.nightcode.snout.shared.domain.usecase.activitylog

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.activitylog.ActivityLogRepository
import com.nightcode.snout.shared.domain.model.activitylog.ActivityEntry
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveActivityLog(
    private val observeSelectedProjectId: ObserveSelectedProjectId,
    private val repository: ActivityLogRepository,
) {
    operator fun invoke(): Flow<List<ActivityEntry>?> =
        observeSelectedProjectId().flatMapLatest { repository.observeActivity(it) }
}

class RefreshActivityLog(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: ActivityLogRepository,
) {
    suspend operator fun invoke(): Either<DataError, Unit> = repository.refreshActivity(getSelectedProjectId())
}
