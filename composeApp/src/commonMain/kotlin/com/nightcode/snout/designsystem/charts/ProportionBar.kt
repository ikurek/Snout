package com.nightcode.snout.designsystem.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.tokens.SnoutTheme

/**
 * A thin full-width bar split into [segments] by share, in order from the start. Decorative: the row it sits in
 * states the numbers in text. Draws nothing when the values add up to zero.
 */
@Composable
internal fun ProportionBar(segments: List<BarSegment>, modifier: Modifier = Modifier) {
    val total = segments.sumOf { it.value.coerceAtLeast(0.0) }
    Canvas(modifier = modifier.fillMaxWidth().height(4.dp).clip(SnoutTheme.shapes.extraSmall)) {
        if (total <= 0) return@Canvas
        var start = 0f
        segments.forEach { segment ->
            val width = size.width * (segment.value.coerceAtLeast(0.0) / total).toFloat()
            drawRect(color = segment.color, topLeft = Offset(start, 0f), size = Size(width, size.height))
            start += width
        }
    }
}

/** One part of a [ProportionBar]: its share is [value] over the sum of all values. */
internal data class BarSegment(val value: Double, val color: Color)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ProportionBarPreview() = ProportionBar(
    segments = listOf(
        BarSegment(6.0, SnoutTheme.colors.error),
        BarSegment(34.0, SnoutTheme.colors.tertiary),
        BarSegment(250.0, SnoutTheme.colors.outline),
        BarSegment(10.0, SnoutTheme.colors.outlineVariant),
    ),
)
