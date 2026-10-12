package com.nightcode.snout.shared.api.analytics.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WebOverviewItemDto(
    @SerialName("key") val key: String,
    @SerialName("value") val value: Double? = null,
    @SerialName("previous") val previous: Double? = null,
    @SerialName("changeFromPreviousPct") val changeFromPreviousPct: Double? = null,
    @SerialName("kind") val kind: String = "unit",
    @SerialName("isIncreaseBad") val isIncreaseBad: Boolean? = null,
)
