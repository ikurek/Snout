package com.nightcode.snout.shared.api.featureflags.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The PATCH body of `feature_flags/{id}`. */
@Serializable
data class UpdateFeatureFlagRequestDto(@SerialName("active") val active: Boolean)
