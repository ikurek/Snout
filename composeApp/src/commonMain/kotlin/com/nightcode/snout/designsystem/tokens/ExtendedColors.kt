package com.nightcode.snout.designsystem.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class SnoutExtendedColors(
    val success: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val info: Color,
    val trendUp: Color,
    val trendDown: Color,
    val trendFlat: Color,
    val chart: List<Color>,
)

internal val LightExtendedColors = SnoutExtendedColors(
    success = SnoutLightTokens.Success,
    successContainer = SnoutLightTokens.SuccessContainer,
    onSuccessContainer = SnoutLightTokens.OnSuccessContainer,
    warning = SnoutLightTokens.Warning,
    warningContainer = SnoutLightTokens.WarningContainer,
    info = SnoutLightTokens.Info,
    trendUp = SnoutLightTokens.TrendUp,
    trendDown = SnoutLightTokens.TrendDown,
    trendFlat = SnoutLightTokens.TrendFlat,
    chart = listOf(
        Color(0xFFBB7400),
        Color(0xFF0074CA),
        Color(0xFFA95CBB),
        Color(0xFF269E5F),
        Color(0xFF00A7B1),
    ),
)

internal val DarkExtendedColors = SnoutExtendedColors(
    success = SnoutDarkTokens.Success,
    successContainer = SnoutDarkTokens.SuccessContainer,
    onSuccessContainer = SnoutDarkTokens.OnSuccessContainer,
    warning = SnoutDarkTokens.Warning,
    warningContainer = SnoutDarkTokens.WarningContainer,
    info = SnoutDarkTokens.Info,
    trendUp = SnoutDarkTokens.TrendUp,
    trendDown = SnoutDarkTokens.TrendDown,
    trendFlat = SnoutDarkTokens.TrendFlat,
    chart = listOf(
        Color(0xFFEBA941),
        Color(0xFF59AAF8),
        Color(0xFFD787E9),
        Color(0xFF5EC386),
        Color(0xFF00C9D1),
    ),
)

internal val LocalSnoutExtendedColors = staticCompositionLocalOf { LightExtendedColors }
