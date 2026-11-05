package com.nightcode.snout.shared.common.format

import com.nightcode.snout.shared.domain.model.insights.ValueFormat
import com.nightcode.snout.shared.domain.model.insights.ValueUnit

private const val MillisPerSecond = 1_000.0
private const val PercentDecimals = 1

/** Writes an insight value the way PostHog's y-axis format asks for: 46 seconds -> "46s", 0.46 scaled -> "46%". */
fun Double.formatValue(format: ValueFormat): String {
    val body = when (format.unit) {
        ValueUnit.Numeric -> format.decimalPlaces?.let { formatDecimals(it) } ?: formatCompact()
        ValueUnit.Duration -> formatDurationSeconds()
        ValueUnit.DurationMs -> (this / MillisPerSecond).formatDurationSeconds()
        ValueUnit.Percentage -> "${formatDecimals(format.decimalPlaces ?: PercentDecimals)}%"
        ValueUnit.PercentageScaled -> "${(this * 100).formatDecimals(format.decimalPlaces ?: PercentDecimals)}%"
    }
    return "${format.prefix.orEmpty()}$body${format.postfix.orEmpty()}"
}
