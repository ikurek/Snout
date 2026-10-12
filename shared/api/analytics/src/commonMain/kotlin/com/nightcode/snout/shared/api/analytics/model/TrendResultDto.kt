package com.nightcode.snout.shared.api.analytics.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** The series definition PostHog echoes back with each result, used to tell result items apart. */
@Serializable
data class TrendActionDto(
    @SerialName("custom_name") val customName: String? = null,
    @SerialName("math") val math: String? = null,
    @SerialName("event") val event: String? = null,
)

/** One series of a TrendsQuery result. */
@Serializable
data class TrendResultDto(
    @SerialName("label") val label: String? = null,
    @SerialName("data") val data: List<Double>? = null,
    @SerialName("labels") val labels: List<String>? = null,
    @SerialName("days") val days: List<String>? = null,
    @SerialName("count") val count: Double? = null,
    @SerialName("aggregated_value") val aggregatedValue: Double? = null,
    @SerialName("compare_label") val compareLabel: String? = null,
    @SerialName("breakdown_value") val breakdownValue: JsonElement? = null,
    @SerialName("action") val action: TrendActionDto? = null,
)
