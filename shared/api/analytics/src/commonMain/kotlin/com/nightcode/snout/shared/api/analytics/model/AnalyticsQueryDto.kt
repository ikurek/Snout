package com.nightcode.snout.shared.api.analytics.model

import com.nightcode.snout.shared.api.DateRangeDto
import com.nightcode.snout.shared.api.PropertyFilterDto
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Each query names its PostHog `kind` in a body property that is always encoded, since it never varies.

@Serializable
data class WebOverviewQueryDto(
    @SerialName("dateRange") val dateRange: DateRangeDto,
    @SerialName("properties") val properties: List<PropertyFilterDto>,
    @SerialName("compareFilter") val compareFilter: CompareFilterDto,
) {
    @EncodeDefault
    @SerialName("kind")
    val kind: String = "WebOverviewQuery"
}

@Serializable
data class WebStatsTableQueryDto(
    @SerialName("breakdownBy") val breakdownBy: String,
    @SerialName("dateRange") val dateRange: DateRangeDto,
    @SerialName("properties") val properties: List<PropertyFilterDto>,
    @SerialName("limit") val limit: Int,
    @SerialName("compareFilter") val compareFilter: CompareFilterDto,
) {
    @EncodeDefault
    @SerialName("kind")
    val kind: String = "WebStatsTableQuery"
}

@Serializable
data class TrendsQueryDto(
    @SerialName("dateRange") val dateRange: DateRangeDto,
    @SerialName("interval") val interval: String,
    @SerialName("series") val series: List<EventsNodeDto>,
    @SerialName("trendsFilter") val trendsFilter: TrendsFilterDto,
    @SerialName("compareFilter") val compareFilter: CompareFilterDto? = null,
    @SerialName("breakdownFilter") val breakdownFilter: BreakdownFilterDto? = null,
) {
    @EncodeDefault
    @SerialName("kind")
    val kind: String = "TrendsQuery"
}

/** A trends series over one event; PostHog echoes [customName] back so results can be told apart. */
@Serializable
data class EventsNodeDto(
    @SerialName("event") val event: String,
    @SerialName("name") val name: String,
    @SerialName("custom_name") val customName: String,
    @SerialName("math") val math: String,
) {
    @EncodeDefault
    @SerialName("kind")
    val kind: String = "EventsNode"
}

@Serializable
data class CompareFilterDto(@SerialName("compare") val compare: Boolean)

@Serializable
data class TrendsFilterDto(@SerialName("display") val display: String)

@Serializable
data class BreakdownFilterDto(
    @SerialName("breakdown") val breakdown: String,
    @SerialName("breakdown_type") val breakdownType: String,
    @SerialName("breakdown_limit") val breakdownLimit: Int? = null,
)
