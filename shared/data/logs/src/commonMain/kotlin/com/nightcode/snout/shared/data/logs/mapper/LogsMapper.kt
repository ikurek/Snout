package com.nightcode.snout.shared.data.logs.mapper

import com.nightcode.snout.shared.api.logs.model.LogEntryDto
import com.nightcode.snout.shared.api.logs.model.LogServicesResponseDto
import com.nightcode.snout.shared.api.logs.model.LogVolumeBucketDto
import com.nightcode.snout.shared.api.logs.model.LogsQueryResponseDto
import com.nightcode.snout.shared.domain.model.logs.LogEntry
import com.nightcode.snout.shared.domain.model.logs.LogLevelCounts
import com.nightcode.snout.shared.domain.model.logs.LogPage
import com.nightcode.snout.shared.domain.model.logs.LogService
import com.nightcode.snout.shared.domain.model.logs.LogSeverity
import com.nightcode.snout.shared.domain.model.logs.LogSeverityFilter
import com.nightcode.snout.shared.domain.model.logs.LogVolume
import com.nightcode.snout.shared.domain.model.logs.LogsRange
import kotlin.time.Instant

/** PostHog pads unset trace and span IDs with zeros rather than leaving them out. */
private val EmptyId = Regex("^0*$")

/** The four levels PostHog groups volume by, in the order they stack from the baseline. */
private val VolumeLevels = listOf(LogSeverity.Error, LogSeverity.Warn, LogSeverity.Info, LogSeverity.Debug)

internal val LogsRange.dateFrom: String
    get() = when (this) {
        LogsRange.LastHour -> "-1h"
        LogsRange.Last24Hours -> "-24h"
        LogsRange.Last7Days -> "-7d"
    }

internal val LogSeverityFilter.apiLevels: List<String> get() = levels.map { it.name.lowercase() }

internal fun LogServicesResponseDto.toServices(): List<LogService> {
    // ISO 8601 times sort chronologically as strings.
    val volumes = sparkline.groupBy { it.serviceName }
        .mapValues { (_, buckets) -> buckets.sortedBy { it.time }.map { it.count } }
    return services.map { dto ->
        LogService(
            name = dto.serviceName,
            logCount = dto.logCount,
            errorCount = dto.errorCount,
            errorRate = dto.errorRate,
            volume = volumes[dto.serviceName].orEmpty(),
            levels = dto.severityBreakdown?.let { LogLevelCounts(it.debug, it.info, it.warn, it.error) },
        )
    }
}

/** Entries whose timestamp can't be read are dropped rather than shown out of order. */
internal fun LogsQueryResponseDto.toPage(): LogPage = LogPage(
    entries = results.mapNotNull { it.toEntry() },
    nextCursor = nextCursor?.takeIf { hasMore },
)

private fun LogEntryDto.toEntry(): LogEntry? {
    val instant = runCatching { Instant.parse(timestamp) }.getOrNull() ?: return null
    return LogEntry(
        id = uuid,
        timestamp = instant,
        body = body,
        severity = severityText.toSeverity(),
        traceId = traceId?.takeUnless { EmptyId.matches(it) },
        spanId = spanId?.takeUnless { EmptyId.matches(it) },
        logger = instrumentationScope?.takeIf { it.isNotBlank() },
        eventName = eventName?.takeIf { it.isNotBlank() },
        attributes = attributes,
        resourceAttributes = resourceAttributes,
    )
}

/**
 * Buckets come one per level per time slot, with slots missing where a level logged nothing. Levels are folded into
 * PostHog's four groups and every series gets a value for every slot.
 */
internal fun List<LogVolumeBucketDto>.toVolume(): LogVolume {
    // ISO 8601 times sort chronologically as strings; slots whose time can't be read are dropped from every series.
    val slots = map { it.time }.distinct().sorted()
        .mapNotNull { time -> runCatching { Instant.parse(time) }.getOrNull()?.let { time to it } }
    val counts = VolumeLevels.associateWith { level ->
        val byTime = filter { it.severity.toSeverity().volumeLevel() == level }
            .groupBy { it.time }
            .mapValues { (_, buckets) -> buckets.sumOf { it.count } }
        slots.map { (time, _) -> byTime[time] ?: 0L }
    }
    return LogVolume(buckets = slots.map { it.second }, counts = counts)
}

private fun LogSeverity.volumeLevel(): LogSeverity? = when (this) {
    LogSeverity.Trace, LogSeverity.Debug -> LogSeverity.Debug
    LogSeverity.Info -> LogSeverity.Info
    LogSeverity.Warn -> LogSeverity.Warn
    LogSeverity.Error, LogSeverity.Fatal -> LogSeverity.Error
    LogSeverity.Unknown -> null
}

private fun String?.toSeverity(): LogSeverity = when (this?.lowercase()) {
    "trace" -> LogSeverity.Trace
    "debug" -> LogSeverity.Debug
    "info" -> LogSeverity.Info
    "warn", "warning" -> LogSeverity.Warn
    "error" -> LogSeverity.Error
    "fatal", "critical" -> LogSeverity.Fatal
    else -> LogSeverity.Unknown
}
