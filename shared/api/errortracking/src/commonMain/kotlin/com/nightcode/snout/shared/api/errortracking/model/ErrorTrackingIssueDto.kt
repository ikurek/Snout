package com.nightcode.snout.shared.api.errortracking.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.time.Instant

@Serializable
data class ErrorTrackingIssuesResponseDto(
    @SerialName("results") val results: List<ErrorTrackingIssueDto> = emptyList(),
    @SerialName("hasMore") val hasMore: Boolean = false,
)

/** A row of `error_tracking/query/issues`; `error_tracking/query/issue` adds the detail-only fields. */
@Serializable
data class ErrorTrackingIssueDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String? = null,
    /** Truncated to a short preview in list rows, complete in the detail response. */
    @SerialName("description") val description: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("severity") val severity: String? = null,
    @SerialName("first_seen") val firstSeen: Instant? = null,
    @SerialName("last_seen") val lastSeen: Instant? = null,
    @SerialName("library") val library: String? = null,
    @SerialName("source") val source: String? = null,
    @SerialName("function") val function: String? = null,
    /** Only in list rows; the detail response carries [impact] and [sparkline] instead. */
    @SerialName("aggregations") val aggregations: ErrorTrackingAggregationsDto? = null,
    @SerialName("impact") val impact: ErrorTrackingAggregationsDto? = null,
    @SerialName("sparkline") val sparkline: List<Double>? = null,
    @SerialName("top_in_app_frame") val topInAppFrame: ErrorTrackingFrameDto? = null,
    @SerialName("latest_release") val latestRelease: ErrorTrackingReleaseDto? = null,
)

@Serializable
data class ErrorTrackingAggregationsDto(
    @SerialName("occurrences") val occurrences: Double? = null,
    @SerialName("users") val users: Double? = null,
    @SerialName("sessions") val sessions: Double? = null,
    /** Occurrences per equal-width bucket across the requested range, oldest first. */
    @SerialName("volumeRange") val volumeRange: List<Double>? = null,
)

@Serializable
data class ErrorTrackingFrameDto(
    @SerialName("function") val function: String? = null,
    @SerialName("source") val source: String? = null,
    @SerialName("line") val line: Int? = null,
    @SerialName("column") val column: Int? = null,
)

@Serializable
data class ErrorTrackingReleaseDto(
    @SerialName("version") val version: String? = null,
    @SerialName("commit_id") val commitId: String? = null,
    @SerialName("branch") val branch: String? = null,
)

/** Rows of a HogQL query, one array of column values per row in SELECT order. */
@Serializable
data class HogQLResultsDto(@SerialName("results") val results: List<List<JsonElement>> = emptyList())
