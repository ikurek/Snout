package com.nightcode.snout.designsystem.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.format.formatCompact
import com.nightcode.snout.common.format.formatPercent
import com.nightcode.snout.designsystem.tokens.SnoutTheme

private const val FullCircle = 360f
private const val StartAngle = -90f
private const val GapDegrees = 2f
private const val MaxLegendRows = 6

@Immutable
internal data class ChartSlice(val label: String, val value: Double, val color: Color)

@Composable
internal fun DonutChart(
    slices: List<ChartSlice>,
    modifier: Modifier = Modifier,
    valueFormatter: (Double) -> String = { it.formatCompact() },
) {
    val total = slices.sumOf { it.value }.takeIf { it > 0 } ?: 1.0
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(132.dp)) {
                val stroke = 18.dp.toPx()
                val arcSize = Size(size.width - stroke, size.height - stroke)
                val topLeft = Offset(stroke / 2, stroke / 2)
                var start = StartAngle
                slices.forEach { slice ->
                    val sweep = (slice.value / total).toFloat() * FullCircle
                    drawArc(
                        color = slice.color,
                        startAngle = start,
                        sweepAngle = (sweep - GapDegrees).coerceAtLeast(0f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke),
                    )
                    start += sweep
                }
            }
            Text(text = valueFormatter(total), style = SnoutTheme.code.medium)
        }
        Column(
            modifier = Modifier.weight(1f).padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            slices.take(MaxLegendRows).forEach { slice ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(modifier = Modifier.size(10.dp).background(slice.color, SnoutTheme.shapes.extraSmall))
                    Text(
                        text = slice.label,
                        style = SnoutTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = (slice.value / total).formatPercent(isFraction = true),
                        style = SnoutTheme.code.small,
                        color = SnoutTheme.colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun DonutChartPreview() = DonutChart(
    slices = listOf(
        ChartSlice("Chrome", 620.0, chartColor(0)),
        ChartSlice("Safari", 280.0, chartColor(1)),
        ChartSlice("Firefox", 100.0, chartColor(2)),
    ),
)
