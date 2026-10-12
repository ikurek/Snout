package com.nightcode.snout.shared.api.errortracking.model

import com.nightcode.snout.shared.api.DateRangeDto
import com.nightcode.snout.shared.api.PropertyFilterDto
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The body of `error_tracking/query/issues`. */
@Serializable
data class ErrorTrackingIssuesRequestDto(
    @SerialName("status") val status: String,
    @SerialName("orderBy") val orderBy: String,
    @SerialName("limit") val limit: Int,
    @SerialName("volumeResolution") val volumeResolution: Int,
    @SerialName("dateRange") val dateRange: DateRangeDto,
    @SerialName("searchQuery") val searchQuery: String? = null,
    @SerialName("filterGroup") val filterGroup: List<PropertyFilterDto>? = null,
)

/** The body of `error_tracking/query/issue`. */
@Serializable
data class ErrorTrackingIssueRequestDto(
    @SerialName("issueId") val issueId: String,
    @SerialName("includeSparkline") val includeSparkline: Boolean,
    @SerialName("volumeResolution") val volumeResolution: Int,
    @SerialName("dateRange") val dateRange: DateRangeDto,
)

/** The PATCH body of `error_tracking/issues/{id}`. */
@Serializable
data class UpdateErrorTrackingIssueRequestDto(@SerialName("status") val status: String)

@Serializable
data class HogQLQueryDto(@SerialName("query") val query: String) {
    @EncodeDefault
    @SerialName("kind")
    val kind: String = "HogQLQuery"
}
