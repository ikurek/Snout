package com.nightcode.snout.shared.api.annotations.model

import com.nightcode.snout.shared.api.UserBasicDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class AnnotationDto(
    @SerialName("id") val id: Long,
    @SerialName("content") val content: String? = null,
    @SerialName("date_marker") val dateMarker: Instant? = null,
    @SerialName("scope") val scope: String? = null,
    @SerialName("created_by") val createdBy: UserBasicDto? = null,
    @SerialName("created_at") val createdAt: Instant? = null,
    @SerialName("dashboard_name") val dashboardName: String? = null,
    @SerialName("insight_name") val insightName: String? = null,
    @SerialName("deleted") val deleted: Boolean = false,
)

@Serializable
data class CreateAnnotationDto(
    @SerialName("content") val content: String,
    @SerialName("date_marker") val dateMarker: Instant,
    @SerialName("scope") val scope: String = "project",
)
