package com.nightcode.snout.shared.data.logs

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.nightcode.snout.shared.api.logs.LogsApi
import com.nightcode.snout.shared.data.common.toDataError
import com.nightcode.snout.shared.data.logs.mapper.apiLevels
import com.nightcode.snout.shared.data.logs.mapper.dateFrom
import com.nightcode.snout.shared.data.logs.mapper.toPage
import com.nightcode.snout.shared.data.logs.mapper.toServices
import com.nightcode.snout.shared.data.logs.mapper.toVolume
import com.nightcode.snout.shared.domain.data.logs.LogRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.logs.LogEntry
import com.nightcode.snout.shared.domain.model.logs.LogPage
import com.nightcode.snout.shared.domain.model.logs.LogService
import com.nightcode.snout.shared.domain.model.logs.LogSeverityFilter
import com.nightcode.snout.shared.domain.model.logs.LogVolume
import com.nightcode.snout.shared.domain.model.logs.LogsRange
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/** PostHog stores timestamps to the microsecond; a second either side always contains the line. */
private val LookupWindow = 1.seconds

@Single(binds = [LogRepository::class])
internal class LogRepositoryImpl(private val api: LogsApi) : LogRepository {

    override suspend fun getServices(projectId: Long, range: LogsRange): Either<DataError, List<LogService>> =
        api.getServices(projectId, range.dateFrom)
            .mapLeft { it.toDataError() }
            .map { it.toServices() }

    override suspend fun getLogs(
        projectId: Long,
        service: String,
        range: LogsRange,
        severity: LogSeverityFilter,
        query: String,
        cursor: String?,
    ): Either<DataError, LogPage> = api.getLogs(
        projectId = projectId,
        serviceName = service,
        dateFrom = range.dateFrom,
        severityLevels = severity.apiLevels,
        search = query,
        after = cursor,
    ).mapLeft { it.toDataError() }
        .map { it.toPage() }

    override suspend fun getVolume(
        projectId: Long,
        service: String,
        range: LogsRange,
        severity: LogSeverityFilter,
        query: String,
    ): Either<DataError, LogVolume> = api.getVolume(
        projectId = projectId,
        serviceName = service,
        dateFrom = range.dateFrom,
        severityLevels = severity.apiLevels,
        search = query,
    ).mapLeft { it.toDataError() }.map { it.toVolume() }

    override suspend fun getLog(
        projectId: Long,
        service: String,
        id: String,
        timestamp: Instant,
    ): Either<DataError, LogEntry> = api.getLogs(
        projectId = projectId,
        serviceName = service,
        dateFrom = (timestamp - LookupWindow).toString(),
        dateTo = (timestamp + LookupWindow).toString(),
        severityLevels = emptyList(),
        search = null,
        after = null,
    ).mapLeft { it.toDataError() }
        .map { it.toPage().entries }
        .flatMap { entries -> entries.firstOrNull { it.id == id }?.right() ?: DataError.NotFound.left() }
}
