package com.nightcode.snout.shared.data.experiments.mapper

import com.nightcode.snout.shared.api.experiments.model.ExperimentDto
import com.nightcode.snout.shared.domain.model.experiments.Experiment
import com.nightcode.snout.shared.domain.model.experiments.ExperimentStatus
import com.nightcode.snout.shared.domain.model.experiments.ExperimentVariant
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

internal fun ExperimentDto.toExperiment(): Experiment = Experiment(
    id = id,
    name = name,
    description = description?.takeIf { it.isNotBlank() },
    status = when (status) {
        "draft" -> ExperimentStatus.Draft
        "running" -> ExperimentStatus.Running
        "paused" -> ExperimentStatus.Paused
        "exposure_frozen" -> ExperimentStatus.ExposureFrozen
        "stopped", "complete" -> ExperimentStatus.Stopped
        null -> derivedStatus()
        else -> ExperimentStatus.Unknown
    },
    startDate = startDate,
    endDate = endDate,
    featureFlagKey = featureFlagKey ?: featureFlag?.key,
    featureFlagId = featureFlag?.id,
    variants = featureFlag?.filters?.multivariate?.variants.orEmpty().map {
        ExperimentVariant(it.key, it.name?.takeIf { name -> name.isNotBlank() }, it.rolloutPercentage)
    },
    metricNames = metrics.map { it.metricName() },
    secondaryMetricNames = secondaryMetrics.map { it.metricName() },
    conclusion = conclusion?.takeIf { it.isNotBlank() },
    conclusionComment = conclusionComment?.takeIf { it.isNotBlank() },
    createdBy = createdBy?.displayName?.takeIf { it.isNotBlank() },
    createdAt = createdAt,
    archived = archived,
)

private fun ExperimentDto.derivedStatus() = when {
    startDate == null -> ExperimentStatus.Draft
    endDate == null -> ExperimentStatus.Running
    else -> ExperimentStatus.Stopped
}

/** Metrics are query nodes; prefer the explicit name, then the metric type, then the node kind. */
private fun JsonElement.metricName(): String {
    val node = this as? JsonObject ?: return "Metric"
    return node.string("name")
        ?: node.string("metric_type")?.replaceFirstChar { it.uppercase() }?.let { "$it metric" }
        ?: node.string("kind")
        ?: "Metric"
}

private fun JsonObject.string(key: String): String? = (
    get(
        key,
    ) as? JsonPrimitive
    )?.contentOrNull?.takeIf { it.isNotBlank() }
