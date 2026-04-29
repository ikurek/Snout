package com.nightcode.snout.designsystem.tokens

import androidx.compose.material3.Typography
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

internal val SnoutFontFamily: FontFamily = FontFamily.Default
internal val SnoutMonoFontFamily: FontFamily = FontFamily.Monospace

private val DisplayLarge = TextStyle(
    fontSize = 57.sp,
    lineHeight = 64.sp,
)
private val DisplayMedium = TextStyle(
    fontSize = 45.sp,
    lineHeight = 52.sp,
)
private val DisplaySmall = TextStyle(
    fontSize = 36.sp,
    lineHeight = 44.sp,
)
private val HeadlineLarge = TextStyle(
    fontSize = 32.sp,
    lineHeight = 40.sp,
)
private val HeadlineMedium = TextStyle(
    fontSize = 28.sp,
    lineHeight = 36.sp,
)
private val HeadlineSmall = TextStyle(
    fontSize = 24.sp,
    lineHeight = 32.sp,
)
private val TitleLarge = TextStyle(
    fontSize = 22.sp,
    lineHeight = 28.sp,
    fontWeight = FontWeight.Medium,
)
private val TitleMedium = TextStyle(
    fontSize = 16.sp,
    lineHeight = 24.sp,
    fontWeight = FontWeight.Medium,
)
private val TitleSmall = TextStyle(
    fontSize = 14.sp,
    lineHeight = 20.sp,
    fontWeight = FontWeight.Medium,
)
private val BodyLarge = TextStyle(
    fontSize = 16.sp,
    lineHeight = 24.sp,
)
private val BodyMedium = TextStyle(
    fontSize = 14.sp,
    lineHeight = 20.sp,
)
private val BodySmall = TextStyle(
    fontSize = 12.sp,
    lineHeight = 16.sp,
)
private val LabelLarge = TextStyle(
    fontSize = 14.sp,
    lineHeight = 20.sp,
    fontWeight = FontWeight.Medium,
)
private val LabelMedium = TextStyle(
    fontSize = 12.sp,
    lineHeight = 16.sp,
    fontWeight = FontWeight.Medium,
)
private val LabelSmall = TextStyle(
    fontSize = 11.sp,
    lineHeight = 16.sp,
    fontWeight = FontWeight.Medium,
)

internal val SnoutTypography = Typography(
    displayLarge = DisplayLarge.copy(fontFamily = SnoutFontFamily),
    displayMedium = DisplayMedium.copy(fontFamily = SnoutFontFamily),
    displaySmall = DisplaySmall.copy(fontFamily = SnoutFontFamily),
    headlineLarge = HeadlineLarge.copy(fontFamily = SnoutFontFamily),
    headlineMedium = HeadlineMedium.copy(fontFamily = SnoutFontFamily),
    headlineSmall = HeadlineSmall.copy(fontFamily = SnoutFontFamily),
    titleLarge = TitleLarge.copy(fontFamily = SnoutFontFamily),
    titleMedium = TitleMedium.copy(fontFamily = SnoutFontFamily),
    titleSmall = TitleSmall.copy(fontFamily = SnoutFontFamily),
    bodyLarge = BodyLarge.copy(fontFamily = SnoutFontFamily),
    bodyMedium = BodyMedium.copy(fontFamily = SnoutFontFamily),
    bodySmall = BodySmall.copy(fontFamily = SnoutFontFamily),
    labelLarge = LabelLarge.copy(fontFamily = SnoutFontFamily),
    labelMedium = LabelMedium.copy(fontFamily = SnoutFontFamily),
    labelSmall = LabelSmall.copy(fontFamily = SnoutFontFamily),
)

internal val SnoutMonoTypography = Typography(
    displayLarge = DisplayLarge.copy(fontFamily = SnoutMonoFontFamily),
    displayMedium = DisplayMedium.copy(fontFamily = SnoutMonoFontFamily),
    displaySmall = DisplaySmall.copy(fontFamily = SnoutMonoFontFamily),
    headlineLarge = HeadlineLarge.copy(fontFamily = SnoutMonoFontFamily),
    headlineMedium = HeadlineMedium.copy(fontFamily = SnoutMonoFontFamily),
    headlineSmall = HeadlineSmall.copy(fontFamily = SnoutMonoFontFamily),
    titleLarge = TitleLarge.copy(fontFamily = SnoutMonoFontFamily),
    titleMedium = TitleMedium.copy(fontFamily = SnoutMonoFontFamily),
    titleSmall = TitleSmall.copy(fontFamily = SnoutMonoFontFamily),
    bodyLarge = BodyLarge.copy(fontFamily = SnoutMonoFontFamily),
    bodyMedium = BodyMedium.copy(fontFamily = SnoutMonoFontFamily),
    bodySmall = BodySmall.copy(fontFamily = SnoutMonoFontFamily),
    labelLarge = LabelLarge.copy(fontFamily = SnoutMonoFontFamily),
    labelMedium = LabelMedium.copy(fontFamily = SnoutMonoFontFamily),
    labelSmall = LabelSmall.copy(fontFamily = SnoutMonoFontFamily),
)

internal val LocalSnoutMonoTypography = staticCompositionLocalOf { SnoutMonoTypography }
