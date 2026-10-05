package com.nightcode.snout.shared.api.experiments.model

import com.nightcode.snout.shared.api.UserBasicDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.time.Instant

@Serializable
data class ExperimentDto(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String? = null,
    @SerialName("start_date") val startDate: Instant? = null,
    @SerialName("end_date") val endDate: Instant? = null,
    @SerialName("archived") val archived: Boolean = false,
    @SerialName("deleted") val deleted: Boolean = false,
    @SerialName("feature_flag_key") val featureFlagKey: String? = null,
    @SerialName("feature_flag") val featureFlag: ExperimentFlagDto? = null,
    @SerialName("parameters") val parameters: JsonElement? = null,
    @SerialName("metrics") val metrics: List<JsonElement> = emptyList(),
    @SerialName("metrics_secondary") val secondaryMetrics: List<JsonElement> = emptyList(),
    @SerialName("status") val status: String? = null,
    @SerialName("conclusion") val conclusion: String? = null,
    @SerialName("conclusion_comment") val conclusionComment: String? = null,
    @SerialName("created_by") val createdBy: UserBasicDto? = null,
    @SerialName("created_at") val createdAt: Instant? = null,
)

@Serializable
data class ExperimentFlagDto(
    @SerialName("id") val id: Long? = null,
    @SerialName("key") val key: String? = null,
    @SerialName("filters") val filters: ExperimentFlagFiltersDto? = null,
)

@Serializable
data class ExperimentFlagFiltersDto(@SerialName("multivariate") val multivariate: ExperimentMultivariateDto? = null)

@Serializable
data class ExperimentMultivariateDto(@SerialName("variants") val variants: List<ExperimentVariantDto> = emptyList())

@Serializable
data class ExperimentVariantDto(
    @SerialName("key") val key: String,
    @SerialName("name") val name: String? = null,
    @SerialName("rollout_percentage") val rolloutPercentage: Int = 0,
)
