package com.nightcode.snout.shared.api.analytics.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** The series definition PostHog echoes back with each result, used to tell result items apart. */
@Serializable
data class TrendActionDto(
    @SerialName("custom_name") val customName: String? = null,
    val math: String? = null,
    val event: String? = null,
)

/** One series of a TrendsQuery result. */
@Serializable
data class TrendResultDto(
    val label: String? = null,
    val data: List<Double>? = null,
    val labels: List<String>? = null,
    val days: List<String>? = null,
    val count: Double? = null,
    @SerialName("aggregated_value") val aggregatedValue: Double? = null,
    @SerialName("compare_label") val compareLabel: String? = null,
    @SerialName("breakdown_value") val breakdownValue: JsonElement? = null,
    val action: TrendActionDto? = null,
)
