package com.nightcode.snout.designsystem.charts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.shared.common.format.formatCompact

private const val FillAlpha = 0.14f
private const val DashLength = 8f
private const val GapLength = 6f

/**
 * Multi-series line chart with horizontal grid lines and first/middle/last x labels.
 * [xLabels] are already formatted for display. Tapping or dragging picks a period and shows every series' value
 * at it; [selectedIndex] picks one up front, for previews.
 */
@Composable
internal fun LineChart(
    series: List<ChartSeries>,
    xLabels: List<String>,
    modifier: Modifier = Modifier,
    height: Dp = 180.dp,
    fill: Boolean = series.count { it.filled } == 1,
    showLegend: Boolean = series.size > 1,
    valueFormatter: (Double) -> String = { it.formatCompact() },
    selectedIndex: Int? = null,
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = SnoutTheme.typography.bodySmall.copy(color = SnoutTheme.colors.onSurfaceVariant)
    val gridColor = SnoutTheme.colors.outlineVariant
    val markerColor = SnoutTheme.colors.onSurfaceVariant
    val range = remember(series) { axisRange(series.flatMap { it.values }) }
    val count = series.maxOfOrNull { it.values.size } ?: 0
    val selection = remember(series, xLabels) { mutableStateOf(selectedIndex) }

    Column(modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .selectPeriods(selection, key = series) { x, size ->
                        if (count == 0) {
                            null
                        } else {
                            plotRect(size, textMeasurer, labelStyle, range, valueFormatter)
                                .lineIndexAt(x, count)
                        }
                    }
                    // Labels and paths are built once per size and data; picking a period only redraws the marker.
                    .drawWithCache {
                        val axes = measureAxes(size, textMeasurer, labelStyle, range, xLabels, valueFormatter)
                        val lines = series.mapNotNull { linePaths(it, axes.plot, range, fill) }
                        onDrawBehind {
                            drawAxes(axes, gridColor)
                            lines.forEach { drawLinePaths(it) }
                            selection.value?.takeIf { it < count }?.let {
                                drawLineMarker(it, count, series, axes.plot, range, markerColor)
                            }
                        }
                    },
            )
            SelectedPeriodReadout(selection, series, xLabels, count, valueFormatter)
        }
        if (showLegend) {
            // A comparison line drawn in its series' colour shares that series' legend entry.
            ChartLegend(
                entries = remember(series) { series.distinctBy { it.color }.map { it.name to it.color } },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** The measured axis labels and where they go, and the plot rectangle the data is mapped into. */
internal class ChartAxes(val plot: Rect, val gridLines: List<Float>, val labels: List<Pair<TextLayoutResult, Offset>>)

/** Measures and places the grid lines and labels of a chart of [size]. */
internal fun Density.measureAxes(
    size: Size,
    textMeasurer: TextMeasurer,
    labelStyle: TextStyle,
    range: AxisRange,
    xLabels: List<String>,
    valueFormatter: (Double) -> String,
): ChartAxes {
    val plot = plotRect(size, textMeasurer, labelStyle, range, valueFormatter)
    val gridLines = range.gridValues.map { plot.yFor(it, range) }
    val yLabels = range.gridValues.mapIndexed { index, value ->
        val layout = textMeasurer.measure(valueFormatter(value), labelStyle)
        layout to Offset(0f, (gridLines[index] - layout.size.height / 2).coerceAtLeast(0f))
    }
    val labelIndices = listOf(0, (xLabels.size - 1) / 2, xLabels.size - 1).distinct().filter { it >= 0 }
    val xLabelLayouts = labelIndices.mapNotNull { index ->
        val label = xLabels.getOrNull(index) ?: return@mapNotNull null
        val layout = textMeasurer.measure(label, labelStyle)
        val x = plot.xFor(index, xLabels.size)
        val left = when (index) {
            0 -> x
            xLabels.size - 1 -> x - layout.size.width
            else -> x - layout.size.width / 2
        }
        layout to Offset(left.coerceIn(plot.left, plot.right - layout.size.width), plot.bottom + 4.dp.toPx())
    }
    return ChartAxes(plot, gridLines, yLabels + xLabelLayouts)
}

/** Draws the grid lines and labels [measureAxes] placed. */
internal fun DrawScope.drawAxes(axes: ChartAxes, gridColor: Color) {
    axes.gridLines.forEach { y ->
        drawLine(gridColor, Offset(axes.plot.left, y), Offset(axes.plot.right, y), strokeWidth = 1.dp.toPx())
    }
    axes.labels.forEach { (layout, topLeft) -> drawText(layout, topLeft = topLeft) }
}

/** Shows the readout of the picked period; reads the selection itself, so picking recomposes only the readout. */
@Composable
internal fun BoxScope.SelectedPeriodReadout(
    selection: State<Int?>,
    series: List<ChartSeries>,
    xLabels: List<String>,
    count: Int,
    valueFormatter: (Double) -> String,
) {
    selection.value?.takeIf { it < count }?.let { PeriodReadout(series, xLabels, it, count, valueFormatter) }
}

internal fun Rect.xFor(index: Int, count: Int): Float =
    if (count <= 1) left + width / 2 else left + width * index / (count - 1)

internal fun Rect.yFor(value: Double, range: AxisRange): Float = bottom - range.fraction(value) * height

/** Vertical guide at the picked period with a dot on every series that has a value there. */
private fun DrawScope.drawLineMarker(
    index: Int,
    count: Int,
    series: List<ChartSeries>,
    plot: Rect,
    range: AxisRange,
    color: Color,
) {
    val x = plot.xFor(index, count)
    drawLine(color, Offset(x, plot.top), Offset(x, plot.bottom), strokeWidth = 1.dp.toPx())
    series.forEach { s ->
        val value = s.values.getOrNull(index) ?: return@forEach
        val center = Offset(plot.xFor(index, s.values.size), plot.yFor(value, range))
        drawCircle(s.color, radius = 4.dp.toPx(), center = center)
    }
}

/** One series' line, its filled area and its dot when it has a single point, built for a plot. */
private class LinePaths(val color: Color, val line: Path, val area: Path?, val stroke: Stroke, val dot: Offset?)

private fun Density.linePaths(series: ChartSeries, plot: Rect, range: AxisRange, fill: Boolean): LinePaths? {
    if (series.values.isEmpty()) return null
    val path = Path()
    series.values.forEachIndexed { index, value ->
        val x = plot.xFor(index, series.values.size)
        val y = plot.yFor(value, range)
        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    val area = if (fill && series.filled) {
        val zero = plot.yFor(0.0, range)
        Path().apply {
            addPath(path)
            lineTo(plot.xFor(series.values.lastIndex, series.values.size), zero)
            lineTo(plot.xFor(0, series.values.size), zero)
            close()
        }
    } else {
        null
    }
    val dash = if (series.dashed) {
        PathEffect.dashPathEffect(floatArrayOf(DashLength * density, GapLength * density))
    } else {
        null
    }
    return LinePaths(
        color = series.color,
        line = path,
        area = area,
        stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round, pathEffect = dash),
        dot = series.values.singleOrNull()?.let { Offset(plot.xFor(0, 1), plot.yFor(it, range)) },
    )
}

private fun DrawScope.drawLinePaths(paths: LinePaths) {
    paths.area?.let { drawPath(path = it, color = paths.color.copy(alpha = FillAlpha)) }
    drawPath(path = paths.line, color = paths.color, style = paths.stroke)
    paths.dot?.let { drawCircle(paths.color, radius = 4.dp.toPx(), center = it) }
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

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun LineChartSelectedPreview() = LineChart(
    series = listOf(
        ChartSeries("Visitors", listOf(120.0, 180.0, 90.0, 240.0, 200.0, 310.0, 280.0), chartColor(0)),
        ChartSeries(
            name = "Previous period",
            values = listOf(100.0, 150.0, 110.0, 190.0, 170.0, 220.0, 250.0),
            color = chartColor(1),
            dashed = true,
            filled = false,
        ),
    ),
    xLabels = listOf("29 Sep", "30 Sep", "1 Oct", "2 Oct", "3 Oct", "4 Oct", "5 Oct"),
    selectedIndex = 4,
)
