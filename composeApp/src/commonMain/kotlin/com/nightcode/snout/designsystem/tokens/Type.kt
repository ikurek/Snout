package com.nightcode.snout.designsystem.tokens

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Tabular figures so stat values and funnel counts line up column-wise. */
private const val TabularFigures = "tnum"

/**
 * Platform default families: Roboto on Android, SF on iOS, nothing bundled. Material slots map to the design
 * scale as follows: headlineSmall = headline, displaySmall = metric-large, headlineMedium = metric-small,
 * labelSmall = uppercase section labels.
 */
internal val SnoutTypography = Typography(
    headlineSmall = TextStyle(
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.2).sp,
    ),
    titleLarge = TextStyle(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.6.sp,
    ),
    displaySmall = TextStyle(
        fontSize = 28.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.5).sp,
        fontFeatureSettings = TabularFigures,
    ),
    headlineMedium = TextStyle(
        fontSize = 20.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.2).sp,
        fontFeatureSettings = TabularFigures,
    ),
)

/**
 * Monospace is reserved for things a person would paste: flag keys and values, event and property names.
 * [large] is a flag key as a screen title, [medium] is row titles, key-value values and code blocks,
 * [small] is inline code chips.
 */
@Immutable
data class SnoutCodeTypography(val large: TextStyle, val medium: TextStyle, val small: TextStyle)

private val Code = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    fontWeight = FontWeight.Medium,
)

internal val SnoutCodeStyles = SnoutCodeTypography(
    large = Code.copy(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    medium = Code,
    small = Code.copy(fontSize = 12.sp, lineHeight = 16.sp),
)

internal val LocalSnoutCodeTypography = staticCompositionLocalOf { SnoutCodeStyles }
