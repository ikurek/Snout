package com.nightcode.snout.shared.api.analytics.model

import kotlinx.serialization.Serializable

/** One series of a TrendsQuery result. */
@Serializable
data class TrendResultDto(
    val label: String? = null,
    val data: List<Double>? = null,
    val labels: List<String>? = null,
    val days: List<String>? = null,
)
