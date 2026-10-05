package com.nightcode.snout.designsystem.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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

/** Grouped bars per period drawn from the zero line; shares axes with [LineChart]. */
@Composable
internal fun BarChart(
    series: List<ChartSeries>,
    xLabels: List<String>,
    modifier: Modifier = Modifier,
    height: Dp = 180.dp,
    showLegend: Boolean = series.size > 1,
    valueFormatter: (Double) -> String = { it.formatCompact() },
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = SnoutTheme.typography.labelSmall.copy(color = SnoutTheme.colors.onSurfaceVariant)
    val gridColor = SnoutTheme.colors.outlineVariant

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().height(height)) {
            val range = axisRange(series.flatMap { it.values })
            val plot = drawAxes(textMeasurer, labelStyle, gridColor, range, xLabels, valueFormatter)
            val periods = series.maxOfOrNull { it.values.size } ?: 0
            if (periods == 0) return@Canvas
            val groupWidth = plot.width / periods
            val barWidth = groupWidth * GroupFill / series.size
            val radius = CornerRadius(2.dp.toPx())
            val zero = plot.yFor(0.0, range)
            series.forEachIndexed { seriesIndex, s ->
                s.values.forEachIndexed { index, value ->
                    val valueY = plot.yFor(value, range)
                    val left =
                        plot.left + index * groupWidth + groupWidth * (1 - GroupFill) / 2 + seriesIndex * barWidth
                    drawRoundRect(
                        color = s.color,
                        topLeft = Offset(left, min(zero, valueY)),
                        size = Size(barWidth.coerceAtLeast(1f), abs(zero - valueY)),
                        cornerRadius = radius,
                    )
                }
            }
        }
        if (showLegend) {
            ChartLegend(entries = series.map { it.name to it.color }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
private fun BarChartPreview() = BarChart(
    series = listOf(
        ChartSeries("New", listOf(12.0, 18.0, 9.0, 24.0, 20.0, 31.0, 28.0), chartColor(0)),
        ChartSeries("Dormant", listOf(-4.0, -3.0, -8.0, -2.0, -5.0, -1.0, -6.0), chartColor(3)),
    ),
    xLabels = listOf("29 Sep", "30 Sep", "1 Oct", "2 Oct", "3 Oct", "4 Oct", "5 Oct"),
)
