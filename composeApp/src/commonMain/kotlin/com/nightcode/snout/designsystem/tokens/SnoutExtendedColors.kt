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
    success = Color(0xFF276B43),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFD4F0DF),
    onSuccessContainer = Color(0xFF0F3D24),
    chart = listOf(
        Color(0xFFBC3D0A),
        Color(0xFF1D4ED8),
        Color(0xFF0F766E),
        Color(0xFF7E22CE),
        Color(0xFFB45309),
    ),
)

internal val DarkExtendedColors = SnoutExtendedColors(
    success = Color(0xFF7FD79F),
    onSuccess = Color(0xFF0F3D24),
    successContainer = Color(0xFF1E4A30),
    onSuccessContainer = Color(0xFFCFF3DB),
    chart = listOf(
        Color(0xFFFF9466),
        Color(0xFF7EA6FF),
        Color(0xFF4FD1C5),
        Color(0xFFC79BFF),
        Color(0xFFF2C14E),
    ),
)

internal val LocalSnoutExtendedColors = staticCompositionLocalOf { LightExtendedColors }
