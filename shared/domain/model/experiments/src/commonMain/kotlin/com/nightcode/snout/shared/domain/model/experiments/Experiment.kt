package com.nightcode.snout.shared.domain.model.experiments

import com.nightcode.snout.shared.common.testdata.TestData
import kotlinx.serialization.Serializable
import kotlin.time.Instant

enum class ExperimentStatus {
    Draft,
    Running,
    Paused,
    ExposureFrozen,
    Stopped,
    Unknown,
}

@Serializable
data class ExperimentVariant(val key: String, val name: String?, val rolloutPercentage: Int)

@Serializable
data class Experiment(
    val id: Long,
    val name: String,
    val description: String?,
    val status: ExperimentStatus,
    val startDate: Instant?,
    val endDate: Instant?,
    val featureFlagKey: String?,
    val featureFlagId: Long?,
    val variants: List<ExperimentVariant>,
    val metricNames: List<String>,
    val secondaryMetricNames: List<String>,
    val conclusion: String?,
    val conclusionComment: String?,
    val createdBy: String?,
    val createdAt: Instant?,
    val archived: Boolean,
)

@TestData
val TestExperiment = Experiment(
    id = 3L,
    name = "New checkout vs. old",
    description = "Does the redesigned checkout convert better?",
    status = ExperimentStatus.Running,
    startDate = Instant.parse("2026-09-20T08:00:00Z"),
    endDate = null,
    featureFlagKey = "new-checkout",
    featureFlagId = 7L,
    variants = listOf(ExperimentVariant("control", null, 50), ExperimentVariant("test", "New checkout", 50)),
    metricNames = listOf("Purchase conversion", "Revenue per user"),
    secondaryMetricNames = listOf("Checkout errors"),
    conclusion = null,
    conclusionComment = null,
    createdBy = "Jane Doe",
    createdAt = Instant.parse("2026-09-18T10:00:00Z"),
    archived = false,
)
