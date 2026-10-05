package com.nightcode.snout.shared.api.activitylog.model

import com.nightcode.snout.shared.api.UserBasicDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.time.Instant

@Serializable
data class ActivityLogDto(
    @SerialName("id") val id: String,
    @SerialName("user") val user: UserBasicDto? = null,
    @SerialName("activity") val activity: String,
    @SerialName("scope") val scope: String,
    @SerialName("item_id") val itemId: String? = null,
    @SerialName("detail") val detail: ActivityDetailDto? = null,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("is_system") val isSystem: Boolean = false,
)

@Serializable
data class ActivityDetailDto(
    @SerialName("name") val name: String? = null,
    @SerialName("short_id") val shortId: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("changes") val changes: List<ActivityChangeDto>? = null,
)

@Serializable
data class ActivityChangeDto(
    @SerialName("type") val type: String? = null,
    @SerialName("action") val action: String? = null,
    @SerialName("field") val field: String? = null,
    @SerialName("before") val before: JsonElement? = null,
    @SerialName("after") val after: JsonElement? = null,
)
