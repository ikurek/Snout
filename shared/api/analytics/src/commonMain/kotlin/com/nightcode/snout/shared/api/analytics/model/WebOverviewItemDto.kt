package com.nightcode.snout.shared.api.analytics.model

import kotlinx.serialization.Serializable

@Serializable
data class WebOverviewItemDto(
    val key: String,
    val value: Double? = null,
    val previous: Double? = null,
    val changeFromPreviousPct: Double? = null,
    val kind: String = "unit",
    val isIncreaseBad: Boolean? = null,
)
