package com.nightcode.snout.shared.api.errortracking.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ErrorTrackingFingerprintDto(@SerialName("issue_id") val issueId: String)
