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

/** Rounds [value] up to a 1-2-5 step so the top grid line reads cleanly. */
internal fun niceCeiling(value: Double): Double {
    if (value <= 0.0) return 1.0
    var magnitude = 1.0
    while (magnitude * Base <= value) magnitude *= Base
    while (magnitude > value) magnitude /= Base
    val normalized = value / magnitude
    return NiceSteps.first { normalized <= it } * magnitude
}
