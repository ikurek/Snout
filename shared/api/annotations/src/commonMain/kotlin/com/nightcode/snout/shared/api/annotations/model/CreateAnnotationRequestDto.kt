package com.nightcode.snout.shared.api.annotations.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class CreateAnnotationRequestDto(
    @SerialName("content") val content: String,
    @SerialName("date_marker") val dateMarker: Instant,
    @SerialName("scope") val scope: String = "project",
)
