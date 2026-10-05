package com.nightcode.snout.shared.api.insights.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class DashboardDto(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String? = null,
    @SerialName("pinned") val pinned: Boolean = false,
    @SerialName("last_accessed_at") val lastAccessedAt: Instant? = null,
    @SerialName("tags") val tags: List<String> = emptyList(),
)

@Serializable
data class DashboardDetailDto(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String? = null,
    @SerialName("tiles") val tiles: List<DashboardTileDto> = emptyList(),
)

@Serializable
data class DashboardTileDto(
    @SerialName("id") val id: Long,
    @SerialName("insight") val insight: InsightDto? = null,
    @SerialName("text") val text: TextTileDto? = null,
)

@Serializable
data class TextTileDto(@SerialName("body") val body: String = "")
