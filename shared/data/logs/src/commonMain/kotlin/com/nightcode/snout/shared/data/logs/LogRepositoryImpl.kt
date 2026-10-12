package com.nightcode.snout.shared.data.logs

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.nightcode.snout.shared.api.logs.LogsApi
import com.nightcode.snout.shared.data.common.JsonCacheStore
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/** Enough for a few pages of scrolling; older lines are fetched again if opened. */
private const val RecentLogLimit = 1_000

/** PostHog stores timestamps to the microsecond; a second either side always contains the line. */
private val LookupWindow = 1.seconds

@Single(binds = [LogRepository::class])
internal class LogRepositoryImpl(private val api: LogsApi, private val cache: JsonCacheStore) : LogRepository {

    // Lines are too volatile for the disk cache, but a line opened from the list should not cost another request.
    private val recentLock = Mutex()
    private val recent = LinkedHashMap<String, LogEntry>()

    override fun observeServices(projectId: Long, range: LogsRange): Flow<List<LogService>?> =
        cache.observe(key(projectId, range))

    override suspend fun refreshServices(projectId: Long, range: LogsRange): Either<DataError, Unit> =
        api.getServices(projectId, range.dateFrom)
            .mapLeft { it.toDataError() }
            .map { response -> cache.put(key(projectId, range), response.toServices()) }

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
        .onRight { remember(it.entries) }

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
    ): Either<DataError, LogEntry> =
        recentLock.withLock { recent[id] }?.right() ?: findAround(projectId, service, id, timestamp)

    private suspend fun findAround(
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
        .onRight { remember(it) }
        .flatMap { entries -> entries.firstOrNull { it.id == id }?.right() ?: DataError.NotFound.left() }

    private suspend fun remember(entries: List<LogEntry>) = recentLock.withLock {
        entries.forEach { recent[it.id] = it }
        val overflow = recent.size - RecentLogLimit
        if (overflow > 0) recent.keys.take(overflow).forEach(recent::remove)
    }

    private fun key(projectId: Long, range: LogsRange) = "log_services:$projectId:${range.dateFrom}"
}
