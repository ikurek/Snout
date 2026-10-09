package com.nightcode.snout.designsystem.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper

private const val BarFill = 0.7f

/** Minimum bar height as a fraction of the plot, so zero buckets still read as a baseline. */
private const val MinBarFraction = 0.06f

/** Widest slot a bar gets; with few values the bars keep this width and line up at the end, newest last. */
private val MaxSlotWidth = 8.dp

/**
 * Tiny bar sparkline for list rows: no axes, no labels, bars scaled to the largest value. Decorative only; the row
 * states the totals in text.
 */
@Composable
internal fun Sparkline(values: List<Double>, modifier: Modifier = Modifier, color: Color = chartColor(0)) {
    Canvas(modifier = modifier.size(width = 56.dp, height = 24.dp)) {
        if (values.isEmpty()) return@Canvas
        val max = values.max().takeIf { it > 0 } ?: 1.0
        val slot = (size.width / values.size).coerceAtMost(MaxSlotWidth.toPx())
        val start = size.width - slot * values.size
        val barWidth = slot * BarFill
        val radius = CornerRadius(1.dp.toPx())
        values.forEachIndexed { index, value ->
            val height = size.height * ((value / max).toFloat()).coerceAtLeast(MinBarFraction)
            drawRoundRect(
                color = color,
                topLeft = Offset(start + index * slot + (slot - barWidth) / 2, size.height - height),
                size = Size(barWidth, height),
                cornerRadius = radius,
            )
        }
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun SparklinePreview() = Sparkline(values = listOf(96.0, 120.0, 154.0, 0.0, 188.0, 236.0, 280.0))
