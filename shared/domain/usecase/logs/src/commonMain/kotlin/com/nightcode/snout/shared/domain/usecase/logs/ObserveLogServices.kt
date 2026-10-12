package com.nightcode.snout.shared.domain.usecase.logs

import com.nightcode.snout.shared.domain.data.logs.LogRepository
import com.nightcode.snout.shared.domain.model.logs.LogService
import com.nightcode.snout.shared.domain.model.logs.LogsRange
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class ObserveLogServices(
    private val observeSelectedProjectId: ObserveSelectedProjectId,
    private val repository: LogRepository,
) {
    operator fun invoke(range: LogsRange): Flow<List<LogService>?> =
        observeSelectedProjectId.flatMapProject { repository.observeServices(it, range) }
}
