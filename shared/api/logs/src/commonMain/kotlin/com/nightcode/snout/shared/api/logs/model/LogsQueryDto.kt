package com.nightcode.snout.shared.api.logs.model

import com.nightcode.snout.shared.api.DateRangeDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The query of `logs/services`. */
@Serializable
data class LogServicesQueryDto(@SerialName("dateRange") val dateRange: DateRangeDto)

/** The query of `logs/query`. */
@Serializable
data class LogsQueryDto(
    @SerialName("dateRange") val dateRange: DateRangeDto,
    @SerialName("serviceNames") val serviceNames: List<String>,
    @SerialName("severityLevels") val severityLevels: List<String>,
    @SerialName("orderBy") val orderBy: String,
    @SerialName("limit") val limit: Int,
    @SerialName("searchTerm") val searchTerm: String? = null,
    @SerialName("after") val after: String? = null,
)

/** The query of `logs/sparkline`. */
@Serializable
data class LogVolumeQueryDto(
    @SerialName("dateRange") val dateRange: DateRangeDto,
    @SerialName("serviceNames") val serviceNames: List<String>,
    @SerialName("severityLevels") val severityLevels: List<String>,
    @SerialName("sparklineBreakdownBy") val sparklineBreakdownBy: String,
    @SerialName("searchTerm") val searchTerm: String? = null,
)
