package com.nightcode.snout.shared.domain.model.analytics

import kotlinx.serialization.Serializable

enum class OverviewMetricKey {
    Visitors,
    Views,
    Sessions,
    SessionDuration,
    BounceRate,
    Other,
}

enum class OverviewMetricKind {
    Count,
    DurationSeconds,
    Percentage,
}

@Serializable
data class OverviewMetric(
    val key: OverviewMetricKey,
    val label: String,
    val value: Double?,
    val previous: Double?,
    val changePercent: Double?,
    val kind: OverviewMetricKind,
    val isIncreaseBad: Boolean,
)
