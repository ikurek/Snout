package com.nightcode.snout.designsystem.charts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.nightcode.snout.designsystem.tokens.SnoutTheme

@Immutable
internal data class ChartSeries(val name: String, val values: List<Double>, val color: Color)

@Composable
internal fun chartColor(index: Int): Color {
    val colors = SnoutTheme.extendedColors.chart
    return colors[index % colors.size]
}

private const val Base = 10.0
private val NiceSteps = listOf(1.0, 2.0, 5.0, Base)

/** Vertical extent of a chart; spans below zero when any series is negative (lifecycle "dormant"). */
internal data class AxisRange(val min: Double, val max: Double) {
    fun fraction(value: Double): Float = ((value - min) / (max - min)).toFloat()

    /** Grid values: min, zero and max when negative, otherwise 0, half and max. */
    val gridValues: List<Double> get() = if (min < 0) listOf(min, 0.0, max) else listOf(0.0, max / 2, max)
}

internal fun axisRange(values: List<Double>): AxisRange {
    val high = values.maxOrNull()?.coerceAtLeast(0.0) ?: 0.0
    val low = values.minOrNull()?.coerceAtMost(0.0) ?: 0.0
    val top = if (high > 0) niceCeiling(high) else 0.0
    val bottom = if (low < 0) -niceCeiling(-low) else 0.0
    return if (top == 0.0 && bottom == 0.0) AxisRange(0.0, 1.0) else AxisRange(bottom, top)
}

/** Rounds [value] up to a 1-2-5 step so the top grid line reads cleanly. */
internal fun niceCeiling(value: Double): Double {
    if (value <= 0.0) return 1.0
    var magnitude = 1.0
    while (magnitude * Base <= value) magnitude *= Base
    while (magnitude > value) magnitude /= Base
    val normalized = value / magnitude
    return NiceSteps.first { normalized <= it } * magnitude
}
