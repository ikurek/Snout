package com.nightcode.snout.shared.domain.model.analytics

import kotlinx.serialization.Serializable

@Serializable
data class TimeSeriesPoint(val label: String, val value: Double)

@Serializable
data class TimeSeries(val points: List<TimeSeriesPoint>)
