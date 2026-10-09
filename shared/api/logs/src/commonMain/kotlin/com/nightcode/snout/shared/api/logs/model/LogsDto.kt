package com.nightcode.snout.shared.api.logs.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response of `logs/services`: one aggregate per service plus per-service volume buckets for the top 25. */
@Serializable
data class LogServicesResponseDto(
    @SerialName("services") val services: List<LogServiceDto> = emptyList(),
    @SerialName("sparkline") val sparkline: List<LogServiceBucketDto> = emptyList(),
)

@Serializable
data class LogServiceDto(
    @SerialName("service_name") val serviceName: String,
    @SerialName("log_count") val logCount: Long = 0,
    /** Logs at error or fatal severity. */
    @SerialName("error_count") val errorCount: Long = 0,
    @SerialName("error_rate") val errorRate: Double = 0.0,
    @SerialName("severity_breakdown") val severityBreakdown: LogSeverityBreakdownDto? = null,
)

/** Counts per coarse level; [error] includes fatal. */
@Serializable
data class LogSeverityBreakdownDto(
    @SerialName("debug") val debug: Long = 0,
    @SerialName("info") val info: Long = 0,
    @SerialName("warn") val warn: Long = 0,
    @SerialName("error") val error: Long = 0,
)

/** One bucket of `logs/sparkline` broken down by severity. */
@Serializable
data class LogVolumeBucketDto(
    /** Bucket start, ISO 8601. */
    @SerialName("time") val time: String,
    @SerialName("severity") val severity: String? = null,
    @SerialName("count") val count: Long = 0,
)

@Serializable
data class LogServiceBucketDto(
    /** Bucket start, ISO 8601. */
    @SerialName("time") val time: String,
    @SerialName("service_name") val serviceName: String,
    @SerialName("count") val count: Long = 0,
)

/** Response of `logs/query`: newest first, with an opaque cursor for the next page. */
@Serializable
data class LogsQueryResponseDto(
    @SerialName("results") val results: List<LogEntryDto> = emptyList(),
    @SerialName("hasMore") val hasMore: Boolean = false,
    @SerialName("nextCursor") val nextCursor: String? = null,
)

@Serializable
data class LogEntryDto(
    @SerialName("uuid") val uuid: String,
    @SerialName("timestamp") val timestamp: String,
    @SerialName("body") val body: String = "",
    @SerialName("severity_text") val severityText: String? = null,
    /** All zeros when the log is not part of a trace. */
    @SerialName("trace_id") val traceId: String? = null,
    /** All zeros when the log is not part of a span. */
    @SerialName("span_id") val spanId: String? = null,
    /** Usually the logger name; empty when the SDK didn't set one. */
    @SerialName("instrumentation_scope") val instrumentationScope: String? = null,
    @SerialName("event_name") val eventName: String? = null,
    @SerialName("attributes") val attributes: Map<String, String> = emptyMap(),
    /** service.name, host and k8s details; the same for every log from one process. */
    @SerialName("resource_attributes") val resourceAttributes: Map<String, String> = emptyMap(),
)
