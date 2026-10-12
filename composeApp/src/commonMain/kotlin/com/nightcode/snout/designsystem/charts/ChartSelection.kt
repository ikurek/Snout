package com.nightcode.snout.designsystem.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import kotlin.math.roundToInt

private val AxisLabelClearance = 40.dp

/** One series' value at the selected period; [dashed] marks a comparison line. */
@Immutable
internal data class ReadoutEntry(val name: String, val color: Color, val value: String, val dashed: Boolean = false)

/** The plot rectangle [drawAxes] lays the data into, so touch handling maps fingers to the same periods. */
internal fun Density.plotRect(
    size: Size,
    textMeasurer: TextMeasurer,
    labelStyle: TextStyle,
    range: AxisRange,
    valueFormatter: (Double) -> String,
): Rect {
    val yLabelWidth = range.gridValues.maxOf { textMeasurer.measure(valueFormatter(it), labelStyle).size.width }
    val xLabelHeight = textMeasurer.measure("0", labelStyle).size.height + 6.dp.toPx()
    return Rect(
        left = yLabelWidth + 8.dp.toPx(),
        top = 4.dp.toPx(),
        right = size.width - 4.dp.toPx(),
        bottom = size.height - xLabelHeight,
    )
}

/** Nearest point of a line chart with [count] points. */
internal fun Rect.lineIndexAt(x: Float, count: Int): Int =
    if (count <= 1) 0 else ((x - left) / width * (count - 1)).roundToInt().coerceIn(0, count - 1)

/** Bar group under [x] for [count] groups. */
internal fun Rect.barIndexAt(x: Float, count: Int): Int = ((x - left) / (width / count)).toInt().coerceIn(0, count - 1)

/**
 * Tap or drag across a chart to pick the period under the finger; tapping the picked period again clears it.
 * [indexAt] maps an x position in a canvas of the given size to a period, or null when there is none.
 */
internal fun Modifier.selectPeriods(
    selection: MutableState<Int?>,
    key: Any?,
    indexAt: Density.(x: Float, size: Size) -> Int?,
): Modifier = this
    .pointerInput(key) {
        detectTapGestures { offset ->
            val index = indexAt(offset.x, size.toSize())
            selection.value = if (index == selection.value) null else index
        }
    }
    .pointerInput(key) {
        detectHorizontalDragGestures(
            onDragStart = { offset -> selection.value = indexAt(offset.x, size.toSize()) },
        ) { change, _ ->
            change.consume()
            selection.value = indexAt(change.position.x, size.toSize())
        }
    }

/** Every series' value at period [index], in the top corner away from it so the marker stays visible. */
@Composable
internal fun BoxScope.PeriodReadout(
    series: List<ChartSeries>,
    xLabels: List<String>,
    index: Int,
    count: Int,
    valueFormatter: (Double) -> String,
) {
    ChartReadout(
        title = xLabels.getOrNull(index).orEmpty(),
        entries = series.mapNotNull { s ->
            s.values.getOrNull(index)?.let { ReadoutEntry(s.name, s.color, valueFormatter(it), s.dashed) }
        },
        modifier = if (index < count / 2.0) {
            Modifier.align(Alignment.TopEnd).padding(Spacing.xxSmall)
        } else {
            // Clear the y-axis labels on the left.
            Modifier.align(Alignment.TopStart).padding(start = AxisLabelClearance, top = Spacing.xxSmall)
        },
    )
}

/** Floating card naming the picked period and every series' value at it. */
@Composable
internal fun ChartReadout(title: String, entries: List<ReadoutEntry>, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.widthIn(max = 240.dp),
        shape = SnoutTheme.shapes.small,
        color = SnoutTheme.colors.surfaceContainerHighest,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(Spacing.small),
            verticalArrangement = Arrangement.spacedBy(Spacing.xxxSmall),
        ) {
            Text(text = title, style = SnoutTheme.typography.labelMedium, color = SnoutTheme.colors.onSurfaceVariant)
            entries.forEach { entry ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xSmall),
                ) {
                    val swatch = Modifier.size(8.dp).clip(SnoutTheme.shapes.extraSmall)
                    Box(
                        modifier = if (entry.dashed) {
                            swatch.border(1.5.dp, entry.color, SnoutTheme.shapes.extraSmall)
                        } else {
                            swatch.background(entry.color)
                        },
                    )
                    Text(
                        text = entry.name,
                        style = SnoutTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Text(
                        text = entry.value,
                        style = SnoutTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
