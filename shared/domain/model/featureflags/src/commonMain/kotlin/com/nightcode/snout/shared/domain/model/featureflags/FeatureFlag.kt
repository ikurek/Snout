package com.nightcode.snout.shared.domain.model.featureflags

import com.nightcode.snout.shared.common.testdata.TestData
import kotlinx.serialization.Serializable
import kotlin.time.Instant

enum class FeatureFlagStatus {
    Active,
    Inactive,
    Stale,
    Unknown,
}

@Serializable
data class PropertyFilter(val key: String, val operator: String?, val value: String)

@Serializable
data class ReleaseCondition(val rolloutPercentage: Int, val properties: List<PropertyFilter>, val variant: String?)

@Serializable
data class FlagVariant(val key: String, val name: String?, val rolloutPercentage: Int)

@Serializable
data class FeatureFlag(
    val id: Long,
    val key: String,
    val description: String?,
    val active: Boolean,
    val status: FeatureFlagStatus,
    val conditions: List<ReleaseCondition>,
    val variants: List<FlagVariant>,
    val tags: List<String>,
    val createdAt: Instant?,
    val createdBy: String?,
    val canEdit: Boolean,
    val isRemoteConfiguration: Boolean,
    /** Human-readable payloads keyed by [BooleanPayloadKey] or variant key; values are only editable in PostHog. */
    val payloads: Map<String, String> = emptyMap(),
    val hasEncryptedPayloads: Boolean = false,
) {
    val isMultivariate: Boolean get() = variants.isNotEmpty()

    /** Payload served when the flag evaluates to true; for remote configuration flags this is the whole point. */
    val booleanPayload: String? get() = payloads[BooleanPayloadKey]

    fun variantPayload(variantKey: String): String? = payloads[variantKey]

    /** True when a single unconditional group rolls out to everyone. */
    val isReleasedToEveryone: Boolean
        get() = conditions.size == 1 && conditions[0].properties.isEmpty() && conditions[0].rolloutPercentage == 100

    companion object {
        const val BooleanPayloadKey = "true"
    }
}

@TestData
val TestFeatureFlag = FeatureFlag(
    id = 7L,
    key = "new-checkout",
    description = "Redesigned checkout flow",
    active = true,
    status = FeatureFlagStatus.Active,
    conditions = listOf(
        ReleaseCondition(rolloutPercentage = 50, properties = emptyList(), variant = null),
        ReleaseCondition(
            rolloutPercentage = 100,
            properties = listOf(PropertyFilter(key = "email", operator = "icontains", value = "@nightcode.dev")),
            variant = "test",
        ),
    ),
    variants = listOf(FlagVariant("control", "Control", 50), FlagVariant("test", "New checkout", 50)),
    tags = listOf("checkout"),
    createdAt = Instant.parse("2026-09-12T09:30:00Z"),
    createdBy = "Jane Doe",
    canEdit = true,
    isRemoteConfiguration = false,
    payloads = mapOf("test" to "{\n  \"cta\": \"Buy now\"\n}"),
)

@TestData
@OptIn(TestData::class)
val TestRemoteConfigFeatureFlag = TestFeatureFlag.copy(
    id = 9L,
    key = "paywall-config",
    description = "Limits and copy for the paywall",
    conditions = listOf(ReleaseCondition(rolloutPercentage = 100, properties = emptyList(), variant = null)),
    variants = emptyList(),
    tags = emptyList(),
    isRemoteConfiguration = true,
    payloads = mapOf(FeatureFlag.BooleanPayloadKey to "{\n  \"free_views\": 3,\n  \"offering\": \"lifetime_2026\"\n}"),
)
