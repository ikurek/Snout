package com.nightcode.snout.shared.domain.model.analytics

enum class OverviewMetricKey {
    Visitors,
    Views,
    Sessions,
    SessionDuration,
    BounceRate,
    WeeklyActiveUsers,
    MonthlyActiveUsers,
    Other,
}

enum class OverviewMetricKind {
    Count,
    DurationSeconds,
    Percentage,
}

data class OverviewMetric(
    val key: OverviewMetricKey,
    val label: String,
    val value: Double?,
    val previous: Double?,
    val changePercent: Double?,
    val kind: OverviewMetricKind,
    val isIncreaseBad: Boolean,
)
