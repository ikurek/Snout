package com.nightcode.snout.shared.domain.model.analytics

import kotlinx.serialization.Serializable

@Serializable
data class TimeSeriesPoint(val label: String, val value: Double)

@Serializable
data class TimeSeries(val points: List<TimeSeriesPoint>)

/** Metrics the trend chart can plot over the selected range. */
enum class TrendMetric {
    Visitors,
    Views,
    Sessions,
}

/** One metric over time, with the same metric over the preceding period of equal length when PostHog returns it. */
@Serializable
data class MetricTrend(val metric: TrendMetric, val current: TimeSeries, val previous: TimeSeries? = null)
