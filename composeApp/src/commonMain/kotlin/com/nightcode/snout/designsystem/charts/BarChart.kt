package com.nightcode.snout.designsystem.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.format.formatCompact
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import kotlin.math.abs
import kotlin.math.min

private const val GroupFill = 0.7f
private const val HighlightAlpha = 0.12f

/**
 * Grouped bars per period drawn from the zero line; shares axes with [LineChart]. Tapping or dragging picks a
 * period and shows every series' value at it; [selectedIndex] picks one up front, for previews. With [stacked] each
 * period is one bar of the series piled in order from the baseline, for parts of a whole; values must not be negative.
 */
@Composable
internal fun BarChart(
    series: List<ChartSeries>,
    xLabels: List<String>,
    modifier: Modifier = Modifier,
    height: Dp = 180.dp,
    showLegend: Boolean = series.size > 1,
    valueFormatter: (Double) -> String = { it.formatCompact() },
    selectedIndex: Int? = null,
    stacked: Boolean = false,
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = SnoutTheme.typography.bodySmall.copy(color = SnoutTheme.colors.onSurfaceVariant)
    val gridColor = SnoutTheme.colors.outlineVariant
    val highlightColor = SnoutTheme.colors.onSurface.copy(alpha = HighlightAlpha)
    val periods = series.maxOfOrNull { it.values.size } ?: 0
    val range = if (stacked) {
        axisRange((0 until periods).map { index -> series.sumOf { it.values.getOrElse(index) { 0.0 } } })
    } else {
        axisRange(series.flatMap { it.values })
    }
    val selection = remember(series, xLabels) { mutableStateOf(selectedIndex) }
    val selected = selection.value?.takeIf { it < periods }

    Column(modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .selectPeriods(selection, key = series) { x, size ->
                        if (periods == 0) {
                            null
                        } else {
                            plotRect(size, textMeasurer, labelStyle, range, valueFormatter)
                                .barIndexAt(x, periods)
                        }
                    },
            ) {
                val plot = drawAxes(textMeasurer, labelStyle, gridColor, range, xLabels, valueFormatter)
                if (periods == 0) return@Canvas
                val groupWidth = plot.width / periods
                selected?.let {
                    drawRect(
                        color = highlightColor,
                        topLeft = Offset(plot.left + it * groupWidth, plot.top),
                        size = Size(groupWidth, plot.height),
                    )
                }
                if (stacked) {
                    drawStackedBars(series, periods, plot, range, groupWidth)
                } else {
                    drawGroupedBars(series, plot, range, groupWidth)
                }
            }
            selected?.let { PeriodReadout(series, xLabels, it, periods, valueFormatter) }
        }
        if (showLegend) {
            ChartLegend(entries = series.map { it.name to it.color }, modifier = Modifier.fillMaxWidth())
        }
    }
}

/** One rounded bar per series side by side in each period, growing from the zero line in either direction. */
private fun DrawScope.drawGroupedBars(series: List<ChartSeries>, plot: Rect, range: AxisRange, groupWidth: Float) {
    val barWidth = groupWidth * GroupFill / series.size
    val radius = CornerRadius(2.dp.toPx())
    val zero = plot.yFor(0.0, range)
    series.forEachIndexed { seriesIndex, s ->
        s.values.forEachIndexed { index, value ->
            val valueY = plot.yFor(value, range)
            val left = plot.left + index * groupWidth + groupWidth * (1 - GroupFill) / 2 + seriesIndex * barWidth
            drawRoundRect(
                color = s.color,
                topLeft = Offset(left, min(zero, valueY)),
                size = Size(barWidth.coerceAtLeast(1f), abs(zero - valueY)),
                cornerRadius = radius,
            )
        }
    }
}

/** One bar per period with each series' segment on top of the previous ones; segments meet without gaps. */
private fun DrawScope.drawStackedBars(
    series: List<ChartSeries>,
    periods: Int,
    plot: Rect,
    range: AxisRange,
    groupWidth: Float,
) {
    val barWidth = groupWidth * GroupFill
    repeat(periods) { index ->
        val left = plot.left + index * groupWidth + groupWidth * (1 - GroupFill) / 2
        var base = 0.0
        series.forEach { s ->
            val value = s.values.getOrElse(index) { 0.0 }
            if (value <= 0) return@forEach
            val top = plot.yFor(base + value, range)
            drawRect(color = s.color, topLeft = Offset(left, top), size = Size(barWidth, plot.yFor(base, range) - top))
            base += value
        }
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun BarChartPreview() = BarChart(
    series = listOf(
        ChartSeries("New", listOf(12.0, 18.0, 9.0, 24.0, 20.0, 31.0, 28.0), chartColor(0)),
        ChartSeries("Dormant", listOf(-4.0, -3.0, -8.0, -2.0, -5.0, -1.0, -6.0), chartColor(3)),
    ),
    xLabels = listOf("29 Sep", "30 Sep", "1 Oct", "2 Oct", "3 Oct", "4 Oct", "5 Oct"),
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun BarChartSelectedPreview() = BarChart(
    series = listOf(
        ChartSeries("New", listOf(12.0, 18.0, 9.0, 24.0, 20.0, 31.0, 28.0), chartColor(0)),
        ChartSeries("Dormant", listOf(-4.0, -3.0, -8.0, -2.0, -5.0, -1.0, -6.0), chartColor(3)),
    ),
    xLabels = listOf("29 Sep", "30 Sep", "1 Oct", "2 Oct", "3 Oct", "4 Oct", "5 Oct"),
    selectedIndex = 2,
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun BarChartStackedPreview() = BarChart(
    series = listOf(
        ChartSeries("Errors", listOf(2.0, 1.0, 6.0, 3.0, 2.0, 9.0, 4.0), chartColor(0)),
        ChartSeries("Info", listOf(30.0, 42.0, 38.0, 51.0, 47.0, 60.0, 55.0), chartColor(1)),
    ),
    xLabels = listOf("29 Sep", "30 Sep", "1 Oct", "2 Oct", "3 Oct", "4 Oct", "5 Oct"),
    stacked = true,
)
