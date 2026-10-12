package com.nightcode.snout.shared.api.insights.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The PATCH body of `insights/{id}`. */
@Serializable
data class UpdateInsightRequestDto(@SerialName("favorited") val favorited: Boolean)
