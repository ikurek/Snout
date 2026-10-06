package com.nightcode.snout.designsystem.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Colors Material 3 has no role for: the success pair used by positive trends and "active" statuses, and the
 * chart series palette. `chart[0]` equals primary and `chart[1]` equals tertiary so two-series charts read as
 * orange vs blue rather than red vs green.
 */
@Immutable
data class SnoutExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val chart: List<Color>,
)

internal val LightExtendedColors = SnoutExtendedColors(
    success = SnoutLightTokens.Success,
    onSuccess = SnoutLightTokens.OnSuccess,
    successContainer = SnoutLightTokens.SuccessContainer,
    onSuccessContainer = SnoutLightTokens.OnSuccessContainer,
    chart = SnoutLightTokens.Chart,
)

internal val DarkExtendedColors = SnoutExtendedColors(
    success = SnoutDarkTokens.Success,
    onSuccess = SnoutDarkTokens.OnSuccess,
    successContainer = SnoutDarkTokens.SuccessContainer,
    onSuccessContainer = SnoutDarkTokens.OnSuccessContainer,
    chart = SnoutDarkTokens.Chart,
)

internal val LocalSnoutExtendedColors = staticCompositionLocalOf { LightExtendedColors }
