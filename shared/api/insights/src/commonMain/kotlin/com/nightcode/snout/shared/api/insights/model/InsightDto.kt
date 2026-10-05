package com.nightcode.snout.shared.api.insights.model

import com.nightcode.snout.shared.api.UserBasicDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.time.Instant

@Serializable
data class InsightDto(
    @SerialName("id") val id: Long,
    @SerialName("short_id") val shortId: String,
    @SerialName("name") val name: String? = null,
    @SerialName("derived_name") val derivedName: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("favorited") val favorited: Boolean = false,
    @SerialName("query") val query: JsonElement? = null,
    @SerialName("result") val result: JsonElement? = null,
    @SerialName("columns") val columns: List<String>? = null,
    @SerialName("last_modified_at") val lastModifiedAt: Instant? = null,
    @SerialName("created_by") val createdBy: UserBasicDto? = null,
    @SerialName("tags") val tags: List<String> = emptyList(),
)
