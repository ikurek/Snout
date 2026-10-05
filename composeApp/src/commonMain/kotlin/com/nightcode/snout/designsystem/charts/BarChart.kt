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

private const val GroupFill = 0.7f

/** Grouped bars per period; shares axes with [LineChart]. */
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
            val maxValue = niceCeiling(series.maxOfOrNull { s -> s.values.maxOrNull() ?: 0.0 } ?: 0.0)
            val plot = drawAxes(textMeasurer, labelStyle, gridColor, maxValue, xLabels, valueFormatter)
            val periods = series.maxOfOrNull { it.values.size } ?: 0
            if (periods == 0) return@Canvas
            val groupWidth = plot.width / periods
            val barWidth = groupWidth * GroupFill / series.size
            val radius = CornerRadius(2.dp.toPx())
            series.forEachIndexed { seriesIndex, s ->
                s.values.forEachIndexed { index, value ->
                    val barHeight = (value / maxValue).toFloat() * plot.height
                    val left =
                        plot.left + index * groupWidth + groupWidth * (1 - GroupFill) / 2 + seriesIndex * barWidth
                    drawRoundRect(
                        color = s.color,
                        topLeft = Offset(left, plot.bottom - barHeight),
                        size = Size(barWidth.coerceAtLeast(1f), barHeight),
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
        ChartSeries("Returning", listOf(40.0, 35.0, 42.0, 60.0, 45.0, 70.0, 66.0), chartColor(1)),
    ),
    xLabels = listOf("29 Sep", "30 Sep", "1 Oct", "2 Oct", "3 Oct", "4 Oct", "5 Oct"),
)
