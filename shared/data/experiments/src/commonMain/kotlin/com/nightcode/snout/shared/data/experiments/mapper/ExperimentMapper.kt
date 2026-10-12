package com.nightcode.snout.shared.data.experiments.mapper

import com.nightcode.snout.shared.api.experiments.model.ExperimentDto
import com.nightcode.snout.shared.data.common.nullIfBlank
import com.nightcode.snout.shared.data.common.stringValue
import com.nightcode.snout.shared.domain.model.experiments.Experiment
import com.nightcode.snout.shared.domain.model.experiments.ExperimentStatus
import com.nightcode.snout.shared.domain.model.experiments.ExperimentVariant
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

internal fun ExperimentDto.toExperiment(): Experiment = Experiment(
    id = id,
    name = name,
    description = description.nullIfBlank(),
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
    conclusion = conclusion.nullIfBlank(),
    conclusionComment = conclusionComment.nullIfBlank(),
    createdBy = createdBy?.displayName.nullIfBlank(),
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
    return node.stringValue("name").nullIfBlank()
        ?: node.stringValue("metric_type").nullIfBlank()?.replaceFirstChar { it.uppercase() }?.let { "$it metric" }
        ?: node.stringValue("kind").nullIfBlank()
        ?: "Metric"
}
