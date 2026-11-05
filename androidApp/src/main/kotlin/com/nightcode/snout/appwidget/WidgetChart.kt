package com.nightcode.snout.appwidget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding

/** Below this height there is no room for a chart under a widget's number. */
internal val MinChartHeight = 52.dp

/** What the line leaves free at the bottom of a chart, for the footer on its faded fill. */
private val ChartFooterRoom = 34.dp

/** Room at the right edge for the dot on the latest point, so it isn't cut in half. */
private val ChartDotRoom = 4.dp

/**
 * What sits above a [WidgetChart] (the header and the number), inset as a frame would; the chart under it isn't.
 */
@Composable
internal fun WidgetChartTop(content: @Composable ColumnScope.() -> Unit) = Column(
    modifier = GlanceModifier.fillMaxWidth().padding(start = WidgetPadding, top = WidgetPadding, end = WidgetPadding),
    content = content,
)

/**
 * [values] as a chart filling the rest of the widget, [width] by [height], edge to edge and down to the bottom, the
 * line ending on a dot at the right edge, in the app's first chart colour, with when the widget last loaded on its
 * faded fill. The bitmap is drawn at that size, so give the space it will really take.
 */
@Composable
internal fun ColumnScope.WidgetChart(values: List<Double>, width: Dp, height: Dp, loadedAt: Long) {
    val density = LocalContext.current.resources.displayMetrics.density
    Box(
        modifier = GlanceModifier.fillMaxWidth().defaultWeight().padding(top = 4.dp),
        contentAlignment = Alignment.BottomStart,
    ) {
        sparkline(
            values,
            width.value,
            height.value,
            density,
            ChartFooterRoom.scaled().value,
            ChartDotRoom.value,
        )?.let { bitmap ->
            Image(
                provider = ImageProvider(bitmap),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                colorFilter = ColorFilter.tint(WidgetColors.chart),
                modifier = GlanceModifier.fillMaxSize(),
            )
        }
        Box(modifier = GlanceModifier.padding(start = WidgetPadding, bottom = 10.dp)) { WidgetFooter(loadedAt) }
    }
}
