package com.nightcode.snout.shared.api.featureflags.model

import com.nightcode.snout.shared.api.UserBasicDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.time.Instant

@Serializable
data class FeatureFlagDto(
    @SerialName("id") val id: Long,
    @SerialName("key") val key: String,
    @SerialName("name") val name: String? = null,
    @SerialName("active") val active: Boolean = false,
    @SerialName("filters") val filters: FlagFiltersDto? = null,
    @SerialName("created_at") val createdAt: Instant? = null,
    @SerialName("created_by") val createdBy: UserBasicDto? = null,
    @SerialName("tags") val tags: List<String> = emptyList(),
    @SerialName("status") val status: String? = null,
    @SerialName("is_remote_configuration") val isRemoteConfiguration: Boolean = false,
    @SerialName("has_encrypted_payloads") val hasEncryptedPayloads: Boolean = false,
    @SerialName("can_edit") val canEdit: Boolean = true,
    @SerialName("deleted") val deleted: Boolean = false,
)

@Serializable
data class FlagFiltersDto(
    @SerialName("groups") val groups: List<FlagGroupDto> = emptyList(),
    @SerialName("multivariate") val multivariate: FlagMultivariateDto? = null,
    /**
     * Keyed by `"true"` for boolean and remote configuration flags, or by variant key.
     * Values are usually JSON-encoded strings.
     */
    @SerialName("payloads") val payloads: Map<String, JsonElement>? = null,
)

@Serializable
data class FlagGroupDto(
    @SerialName("properties") val properties: List<FlagPropertyDto> = emptyList(),
    @SerialName("rollout_percentage") val rolloutPercentage: Int? = null,
    @SerialName("variant") val variant: String? = null,
)

@Serializable
data class FlagPropertyDto(
    @SerialName("key") val key: String = "",
    @SerialName("value") val value: JsonElement? = null,
    @SerialName("operator") val operator: String? = null,
    @SerialName("type") val type: String? = null,
)

@Serializable
data class FlagMultivariateDto(@SerialName("variants") val variants: List<FlagVariantDto> = emptyList())

@Serializable
data class FlagVariantDto(
    @SerialName("key") val key: String,
    @SerialName("name") val name: String? = null,
    @SerialName("rollout_percentage") val rolloutPercentage: Int = 0,
)
