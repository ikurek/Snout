package com.nightcode.snout.shared.domain.model.logs

import com.nightcode.snout.shared.common.testdata.TestData
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/** How far back the Logs screens look. */
enum class LogsRange {
    LastHour,
    Last24Hours,
    Last7Days,
}

/** OpenTelemetry severity, as PostHog reports it in `severity_text`. */
enum class LogSeverity {
    Trace,
    Debug,
    Info,
    Warn,
    Error,
    Fatal,
    Unknown,
}

/** Severity levels a service's log list can be narrowed to, most permissive first. */
enum class LogSeverityFilter(val levels: List<LogSeverity>) {
    All(emptyList()),
    WarningsAndErrors(listOf(LogSeverity.Warn, LogSeverity.Error, LogSeverity.Fatal)),
    Errors(listOf(LogSeverity.Error, LogSeverity.Fatal)),
}

/** A service sending logs, with its volume in the selected range. */
data class LogService(
    val name: String,
    val logCount: Long,
    /** Logs at error or fatal severity. */
    val errorCount: Long,
    /** [errorCount] divided by [logCount], from 0 to 1. */
    val errorRate: Double,
    /** Log counts per time bucket, oldest first; empty for services outside PostHog's top 25. */
    val volume: List<Long>,
    /** Null when PostHog sent no breakdown. */
    val levels: LogLevelCounts? = null,
)

/** Log counts per coarse level, the way PostHog buckets them: trace counts as debug, fatal as error. */
data class LogLevelCounts(val debug: Long, val info: Long, val warn: Long, val error: Long) {
    val total: Long get() = debug + info + warn + error
}

/**
 * A service's log volume over time: [counts] holds one value per [buckets] entry for each of
 * [LogSeverity.Debug], [LogSeverity.Info], [LogSeverity.Warn] and [LogSeverity.Error], the levels PostHog groups by.
 */
data class LogVolume(val buckets: List<Instant>, val counts: Map<LogSeverity, List<Long>>) {
    val isEmpty: Boolean get() = counts.values.all { series -> series.all { it == 0L } }
}

data class LogEntry(
    val id: String,
    val timestamp: Instant,
    val body: String,
    val severity: LogSeverity,
    /** Null when the log is not part of a trace. */
    val traceId: String?,
    val spanId: String? = null,
    /** Usually the logger name, such as `com.app.PaymentClient`. */
    val logger: String? = null,
    /** Set when the log is an OpenTelemetry event. */
    val eventName: String? = null,
    val attributes: Map<String, String>,
    /** Process-level attributes such as host and k8s details; [attributes] wins when a key appears in both. */
    val resourceAttributes: Map<String, String>,
)

/** One page of logs, newest first; pass [nextCursor] back to load the following page. */
data class LogPage(val entries: List<LogEntry>, val nextCursor: String?)

@TestData
val TestLogServices = listOf(
    LogService(
        "checkout-api",
        logCount = 48_210,
        errorCount = 1_204,
        errorRate = 0.025,
        listOf(320, 410, 380, 520, 610, 590),
        LogLevelCounts(debug = 2_100, info = 41_806, warn = 3_100, error = 1_204),
    ),
    LogService(
        "auth-worker",
        logCount = 9_840,
        errorCount = 12,
        errorRate = 0.0012,
        listOf(140, 160, 150, 170, 165, 180),
        LogLevelCounts(debug = 0, info = 9_528, warn = 300, error = 12),
    ),
    LogService("email-sender", logCount = 412, errorCount = 0, errorRate = 0.0, emptyList()),
)

@TestData
val TestLogEntries = listOf(
    LogEntry(
        id = "0199a1f0-0000-7000-8000-000000000001",
        timestamp = Instant.parse("2026-10-06T11:58:42.120Z"),
        body = "Payment provider timed out after 30000ms (order 48213)",
        severity = LogSeverity.Error,
        traceId = "4bf92f3577b34da6a3ce929d0e0e4736",
        spanId = "00f067aa0ba902b7",
        logger = "com.shop.payments.StripeClient",
        attributes = mapOf("order_id" to "48213", "provider" to "stripe", "http.status_code" to "504"),
        resourceAttributes = mapOf("service.name" to "checkout-api", "host.name" to "checkout-7f9c"),
    ),
    LogEntry(
        id = "0199a1f0-0000-7000-8000-000000000002",
        timestamp = Instant.parse("2026-10-06T11:58:40.004Z"),
        body = "Retrying payment request, attempt 3 of 3",
        severity = LogSeverity.Warn,
        traceId = "4bf92f3577b34da6a3ce929d0e0e4736",
        attributes = mapOf("attempt" to "3"),
        resourceAttributes = mapOf("service.name" to "checkout-api"),
    ),
    LogEntry(
        id = "0199a1f0-0000-7000-8000-000000000003",
        timestamp = Instant.parse("2026-10-06T11:58:31.877Z"),
        body = "POST /v1/orders 201 in 182ms",
        severity = LogSeverity.Info,
        traceId = null,
        attributes = emptyMap(),
        resourceAttributes = mapOf("service.name" to "checkout-api"),
    ),
)

@TestData
val TestLogVolume = LogVolume(
    buckets = (0 until 8).map { Instant.parse("2026-10-06T04:00:00Z") + it.hours },
    counts = mapOf(
        LogSeverity.Error to listOf(12L, 9L, 14L, 80L, 31L, 15L, 10L, 13L),
        LogSeverity.Warn to listOf(40L, 38L, 52L, 120L, 64L, 45L, 41L, 39L),
        LogSeverity.Info to listOf(410L, 380L, 520L, 610L, 590L, 470L, 450L, 430L),
        LogSeverity.Debug to listOf(30L, 25L, 28L, 40L, 33L, 29L, 27L, 31L),
    ),
)
