package com.nightcode.snout.designsystem.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.format.formatCompact
import com.nightcode.snout.designsystem.tokens.SnoutTheme

private const val FillAlpha = 0.14f

/**
 * Multi-series line chart with horizontal grid lines and first/middle/last x labels.
 * [xLabels] are already formatted for display.
 */
@Composable
internal fun LineChart(
    series: List<ChartSeries>,
    xLabels: List<String>,
    modifier: Modifier = Modifier,
    height: Dp = 180.dp,
    fill: Boolean = series.size == 1,
    showLegend: Boolean = series.size > 1,
    valueFormatter: (Double) -> String = { it.formatCompact() },
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = SnoutTheme.typography.bodySmall.copy(color = SnoutTheme.colors.onSurfaceVariant)
    val gridColor = SnoutTheme.colors.outlineVariant

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().height(height)) {
            val range = axisRange(series.flatMap { it.values })
            val plot = drawAxes(textMeasurer, labelStyle, gridColor, range, xLabels, valueFormatter)
            series.forEach { drawLineSeries(it, plot, range, fill) }
        }
        if (showLegend) {
            ChartLegend(entries = series.map { it.name to it.color }, modifier = Modifier.fillMaxWidth())
        }
    }
}

/** Draws grid lines and labels, returning the plot rectangle the data should be mapped into. */
internal fun DrawScope.drawAxes(
    textMeasurer: TextMeasurer,
    labelStyle: TextStyle,
    gridColor: Color,
    range: AxisRange,
    xLabels: List<String>,
    valueFormatter: (Double) -> String,
): Rect {
    val yLabels = range.gridValues
    val yLabelLayouts = yLabels.map { textMeasurer.measure(valueFormatter(it), labelStyle) }
    val yLabelWidth = yLabelLayouts.maxOf { it.size.width } + 8.dp.toPx()
    val xLabelHeight = textMeasurer.measure("0", labelStyle).size.height + 6.dp.toPx()
    val plot = Rect(
        left = yLabelWidth,
        top = 4.dp.toPx(),
        right = size.width - 4.dp.toPx(),
        bottom = size.height - xLabelHeight,
    )
    yLabels.forEachIndexed { index, value ->
        val y = plot.yFor(value, range)
        drawLine(gridColor, Offset(plot.left, y), Offset(plot.right, y), strokeWidth = 1.dp.toPx())
        val layout = yLabelLayouts[index]
        drawText(layout, topLeft = Offset(0f, (y - layout.size.height / 2).coerceAtLeast(0f)))
    }
    val labelIndices = listOf(0, (xLabels.size - 1) / 2, xLabels.size - 1).distinct().filter { it >= 0 }
    labelIndices.forEach { index ->
        val label = xLabels.getOrNull(index) ?: return@forEach
        val layout = textMeasurer.measure(label, labelStyle)
        val x = plot.xFor(index, xLabels.size)
        val left = when (index) {
            0 -> x
            xLabels.size - 1 -> x - layout.size.width
            else -> x - layout.size.width / 2
        }
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(left.coerceIn(plot.left, plot.right - layout.size.width), plot.bottom + 4.dp.toPx()),
        )
    }
    return plot
}

internal fun Rect.xFor(index: Int, count: Int): Float =
    if (count <= 1) left + width / 2 else left + width * index / (count - 1)

internal fun Rect.yFor(value: Double, range: AxisRange): Float = bottom - range.fraction(value) * height

private fun DrawScope.drawLineSeries(series: ChartSeries, plot: Rect, range: AxisRange, fill: Boolean) {
    if (series.values.isEmpty()) return
    val path = Path()
    series.values.forEachIndexed { index, value ->
        val x = plot.xFor(index, series.values.size)
        val y = plot.yFor(value, range)
        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    if (fill) {
        val zero = plot.yFor(0.0, range)
        val area = Path().apply {
            addPath(path)
            lineTo(plot.xFor(series.values.lastIndex, series.values.size), zero)
            lineTo(plot.xFor(0, series.values.size), zero)
            close()
        }
        drawPath(path = area, color = series.color.copy(alpha = FillAlpha))
    }
    drawPath(path, series.color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    if (series.values.size == 1) {
        drawCircle(
            series.color,
            radius = 4.dp.toPx(),
            center = Offset(plot.xFor(0, 1), plot.yFor(series.values[0], range)),
        )
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun LineChartPreview() = LineChart(
    series = listOf(
        ChartSeries("Visitors", listOf(120.0, 180.0, 90.0, 240.0, 200.0, 310.0, 280.0), chartColor(0)),
        ChartSeries("Signups", listOf(20.0, 35.0, 10.0, 60.0, 45.0, 70.0, 66.0), chartColor(1)),
    ),
    xLabels = listOf("29 Sep", "30 Sep", "1 Oct", "2 Oct", "3 Oct", "4 Oct", "5 Oct"),
)
