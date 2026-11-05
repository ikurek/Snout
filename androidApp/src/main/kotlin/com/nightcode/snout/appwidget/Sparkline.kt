package com.nightcode.snout.appwidget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.createBitmap
import kotlin.math.max
import kotlin.math.roundToInt

/** Every bitmap of a widget travels to the launcher in its RemoteViews, so the chart is drawn no wider than this. */
private const val MaxWidthPx = 480
private const val StrokeDp = 2f
private const val DotDp = 3.5f

/** Room above the line, so its peaks aren't cut by the edge of the chart. */
private const val TopDp = 6f

/** Of 255, at the top of the fill; it fades to nothing at the bottom. */
private const val FillAlpha = 72

/**
 * [values] as a chart from edge to edge: a line ending [endInsetDp] short of the right edge, where a dot marks the
 * latest point (whole, not cut by the edge), over a fill that runs to both edges and fades to nothing at the bottom,
 * in white for an `Image` to tint (Glance has no canvas of its own). The line keeps [bottomRoomDp] free at the
 * bottom, where the widget's footer sits on the faded fill. Drawn at the size it is shown at ([widthDp] by
 * [heightDp]), so stretching it to fit doesn't distort the stroke.
 */
internal fun sparkline(
    values: List<Double>,
    widthDp: Float,
    heightDp: Float,
    density: Float,
    bottomRoomDp: Float,
    endInsetDp: Float,
): Bitmap? {
    val scale = minOf(density, MaxWidthPx / widthDp.coerceAtLeast(1f))
    val width = max((widthDp * scale).roundToInt(), 1)
    val height = max((heightDp * scale).roundToInt(), 1)
    val top = TopDp * scale
    val bottom = height - bottomRoomDp * scale
    // No height leaves no room above the footer either.
    if (values.size < 2 || widthDp <= 0 || bottom <= top) return null
    val min = values.min()
    val span = (values.max() - min).takeIf { it > 0 } ?: 1.0
    val stepX = (width - endInsetDp * scale) / (values.size - 1)
    val points = values.mapIndexed { index, value ->
        index * stepX to top + ((1 - (value - min) / span) * (bottom - top)).toFloat()
    }
    val line = Path().apply {
        points.forEachIndexed { index, (x, y) -> if (index == 0) moveTo(x, y) else lineTo(x, y) }
    }
    val area = Path(line).apply {
        lineTo(width.toFloat(), points.last().second)
        lineTo(width.toFloat(), height.toFloat())
        lineTo(0f, height.toFloat())
        close()
    }
    return createBitmap(width, height).also { bitmap ->
        val canvas = Canvas(bitmap)
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            val color = ColorUtils.setAlphaComponent(Color.WHITE, FillAlpha)
            shader = LinearGradient(0f, top, 0f, height.toFloat(), color, Color.TRANSPARENT, Shader.TileMode.CLAMP)
        }
        canvas.drawPath(area, fill)
        canvas.drawPath(
            line,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = StrokeDp * scale
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
            },
        )
        val (lastX, lastY) = points.last()
        canvas.drawCircle(lastX, lastY, DotDp * scale, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE })
    }
}
