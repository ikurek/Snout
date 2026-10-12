package com.nightcode.snout.shared.data.featureflags.mapper

import com.nightcode.snout.shared.api.featureflags.model.FeatureFlagDto
import com.nightcode.snout.shared.api.featureflags.model.FlagGroupDto
import com.nightcode.snout.shared.api.featureflags.model.FlagPropertyDto
import com.nightcode.snout.shared.data.common.nullIfBlank
import com.nightcode.snout.shared.domain.model.featureflags.FeatureFlag
import com.nightcode.snout.shared.domain.model.featureflags.FeatureFlagStatus
import com.nightcode.snout.shared.domain.model.featureflags.FlagVariant
import com.nightcode.snout.shared.domain.model.featureflags.PropertyFilter
import com.nightcode.snout.shared.domain.model.featureflags.ReleaseCondition
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

private const val FullRollout = 100

internal fun FeatureFlagDto.toFeatureFlag(): FeatureFlag = FeatureFlag(
    id = id,
    key = key,
    description = name.nullIfBlank(),
    active = active,
    status = when (status) {
        "ACTIVE" -> FeatureFlagStatus.Active
        "INACTIVE" -> FeatureFlagStatus.Inactive
        "STALE" -> FeatureFlagStatus.Stale
        null -> if (active) FeatureFlagStatus.Active else FeatureFlagStatus.Inactive
        else -> FeatureFlagStatus.Unknown
    },
    conditions = filters?.groups.orEmpty().map { it.toCondition() },
    variants = filters?.multivariate?.variants.orEmpty().map { FlagVariant(it.key, it.name, it.rolloutPercentage) },
    tags = tags,
    createdAt = createdAt,
    createdBy = createdBy?.displayName.nullIfBlank(),
    canEdit = canEdit,
    isRemoteConfiguration = isRemoteConfiguration,
    payloads = filters?.payloads.orEmpty().mapValues { (_, value) -> value.toPayloadText() },
    hasEncryptedPayloads = hasEncryptedPayloads,
)

private val PrettyJson = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
}

/** PostHog stores payloads as JSON-encoded strings; unwrap one level so quoted text reads plain and objects pretty. */
internal fun JsonElement.toPayloadText(): String {
    val decoded = (this as? JsonPrimitive)
        ?.takeIf { it.isString }
        ?.let { raw -> runCatching { Json.parseToJsonElement(raw.content) }.getOrNull() }
        ?: this
    return when (decoded) {
        is JsonPrimitive -> decoded.contentOrNull ?: decoded.toString()
        else -> PrettyJson.encodeToString(JsonElement.serializer(), decoded)
    }
}

private fun FlagGroupDto.toCondition() = ReleaseCondition(
    rolloutPercentage = rolloutPercentage ?: FullRollout,
    properties = properties.map { it.toPropertyFilter() },
    variant = variant,
)

private fun FlagPropertyDto.toPropertyFilter() = PropertyFilter(
    key = key,
    operator = operator,
    value = value.displayText(),
)

private fun JsonElement?.displayText(): String = when (this) {
    null, JsonNull -> ""
    is JsonPrimitive -> contentOrNull ?: toString()
    is JsonArray -> joinToString(", ") { it.displayText() }
    else -> toString()
}
