package com.nightcode.snout.shared.domain.usecase.logs

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.logs.LogsRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.logs.LogEntry
import com.nightcode.snout.shared.domain.model.logs.LogPage
import com.nightcode.snout.shared.domain.model.logs.LogService
import com.nightcode.snout.shared.domain.model.logs.LogSeverityFilter
import com.nightcode.snout.shared.domain.model.logs.LogVolume
import com.nightcode.snout.shared.domain.model.logs.LogsRange
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
class ObserveLogServices(
    private val observeSelectedProjectId: ObserveSelectedProjectId,
    private val repository: LogsRepository,
) {
    operator fun invoke(range: LogsRange): Flow<List<LogService>?> =
        observeSelectedProjectId.flatMapProject { repository.observeServices(it, range) }
}

class RefreshLogServices(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: LogsRepository,
) {
    suspend operator fun invoke(range: LogsRange): Either<DataError, Unit> =
        repository.refreshServices(getSelectedProjectId(), range)
}

class GetServiceLogs(private val getSelectedProjectId: GetSelectedProjectId, private val repository: LogsRepository) {
    suspend operator fun invoke(
        service: String,
        range: LogsRange,
        severity: LogSeverityFilter,
        query: String,
        cursor: String? = null,
    ): Either<DataError, LogPage> = repository.getLogs(getSelectedProjectId(), service, range, severity, query, cursor)
}

class GetLogEntry(private val getSelectedProjectId: GetSelectedProjectId, private val repository: LogsRepository) {
    suspend operator fun invoke(service: String, id: String, timestamp: Instant): Either<DataError, LogEntry> =
        repository.getLog(getSelectedProjectId(), service, id, timestamp)
}

class GetServiceLogVolume(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: LogsRepository,
) {
    suspend operator fun invoke(
        service: String,
        range: LogsRange,
        severity: LogSeverityFilter,
        query: String,
    ): Either<DataError, LogVolume> = repository.getVolume(getSelectedProjectId(), service, range, severity, query)
}
