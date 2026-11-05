package com.nightcode.snout.appwidget

import androidx.compose.ui.graphics.Color
import androidx.glance.color.ColorProvider
import androidx.glance.unit.ColorProvider

/**
 * The "Ember" colours of `composeApp/.../designsystem/tokens` that the widgets use, light by day and dark by night.
 * Glance draws through RemoteViews, so the app's Compose theme can't reach it; keep these in step with the tokens,
 * and [background] with `widget_colors.xml`, which frames widgets before Android 12.
 */
internal object WidgetColors {
    val background: ColorProvider = ColorProvider(day = Color(0xFFFBF7F1), night = Color(0xFF15120E))
    val onBackground: ColorProvider = ColorProvider(day = Color(0xFF1F1A14), night = Color(0xFFF1EAE0))
    val onSurfaceVariant: ColorProvider = ColorProvider(day = Color(0xFF5E554B), night = Color(0xFFB4A99A))

    /** The first chart series, as the app draws its lines. */
    val chart: ColorProvider = ColorProvider(day = Color(0xFFBC3D0A), night = Color(0xFFFF9466))
    val good: ColorProvider = ColorProvider(day = Color(0xFF276B43), night = Color(0xFF7FD79F))
    val bad: ColorProvider = ColorProvider(day = Color(0xFFB3261E), night = Color(0xFFFFB4AB))
}
