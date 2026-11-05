package com.nightcode.snout.shared.common.format

import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKind

/** A web analytics stat as the Analytics tab and the widgets show it: 12.4k visitors, 2m 23s, 42%. */
fun OverviewMetricKind.format(value: Double): String = when (this) {
    OverviewMetricKind.Count -> value.formatCompact()
    OverviewMetricKind.DurationSeconds -> value.formatDurationSeconds()
    OverviewMetricKind.Percentage -> value.formatPercent(isFraction = value <= 1.0)
}
