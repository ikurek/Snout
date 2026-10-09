package com.nightcode.snout.shared.domain.data.logs

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.logs.LogEntry
import com.nightcode.snout.shared.domain.model.logs.LogPage
import com.nightcode.snout.shared.domain.model.logs.LogService
import com.nightcode.snout.shared.domain.model.logs.LogSeverityFilter
import com.nightcode.snout.shared.domain.model.logs.LogVolume
import com.nightcode.snout.shared.domain.model.logs.LogsRange
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

interface LogsRepository {
    fun observeServices(projectId: Long, range: LogsRange): Flow<List<LogService>?>
    suspend fun refreshServices(projectId: Long, range: LogsRange): Either<DataError, Unit>

    /** Skips the disk cache: logs change by the second, so a stale page is no use offline. */
    @Suppress("LongParameterList")
    suspend fun getLogs(
        projectId: Long,
        service: String,
        range: LogsRange,
        severity: LogSeverityFilter,
        query: String,
        cursor: String?,
    ): Either<DataError, LogPage>

    /** The service's volume per level over [range], filtered by [severity] and [query] like [getLogs]. */
    suspend fun getVolume(
        projectId: Long,
        service: String,
        range: LogsRange,
        severity: LogSeverityFilter,
        query: String,
    ): Either<DataError, LogVolume>

    /**
     * One log line by its [id]. PostHog has no endpoint for a single line, so this answers from lines loaded earlier
     * and otherwise searches the service's logs around [timestamp]; [DataError.NotFound] when it has gone.
     */
    suspend fun getLog(projectId: Long, service: String, id: String, timestamp: Instant): Either<DataError, LogEntry>
}
