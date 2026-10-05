package com.nightcode.snout.shared.data.featureflags.mapper

import com.nightcode.snout.shared.api.featureflags.model.FeatureFlagDto
import com.nightcode.snout.shared.api.featureflags.model.FlagGroupDto
import com.nightcode.snout.shared.api.featureflags.model.FlagPropertyDto
import com.nightcode.snout.shared.domain.model.featureflags.FeatureFlag
import com.nightcode.snout.shared.domain.model.featureflags.FeatureFlagStatus
import com.nightcode.snout.shared.domain.model.featureflags.FlagVariant
import com.nightcode.snout.shared.domain.model.featureflags.PropertyFilter
import com.nightcode.snout.shared.domain.model.featureflags.ReleaseCondition
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

private const val FullRollout = 100

internal fun FeatureFlagDto.toFeatureFlag(): FeatureFlag = FeatureFlag(
    id = id,
    key = key,
    description = name?.takeIf { it.isNotBlank() },
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
    createdBy = createdBy?.displayName?.takeIf { it.isNotBlank() },
    canEdit = canEdit,
    isRemoteConfiguration = isRemoteConfiguration,
)

private fun FlagGroupDto.toCondition() = ReleaseCondition(
    rolloutPercentage = rolloutPercentage ?: FullRollout,
    properties = properties.map { it.toPropertyFilter() },
    variant = variant,
)

private fun FlagPropertyDto.toPropertyFilter() = PropertyFilter(
    key = key,
    operator = operator,
    value = value.readable(),
)

private fun JsonElement?.readable(): String = when (this) {
    null, JsonNull -> ""
    is JsonPrimitive -> contentOrNull ?: toString()
    is JsonArray -> joinToString(", ") { it.readable() }
    else -> toString()
}
