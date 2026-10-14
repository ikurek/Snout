package com.nightcode.snout.designsystem.tokens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/** Ember palette, light theme. */
private val LightColors = lightColorScheme(
    primary = Color(0xFFBC3D0A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDBCC),
    onPrimaryContainer = Color(0xFF5A1E00),
    secondary = Color(0xFF705B3B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF3DFC6),
    onSecondaryContainer = Color(0xFF3F2B12),
    tertiary = Color(0xFF1D4ED8),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFDCE3FF),
    onTertiaryContainer = Color(0xFF0B1F6B),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFBF7F1),
    onBackground = Color(0xFF1F1A14),
    surface = Color(0xFFFBF7F1),
    onSurface = Color(0xFF1F1A14),
    surfaceVariant = Color(0xFFDED4C6),
    onSurfaceVariant = Color(0xFF5E554B),
    outline = Color(0xFF857A6D),
    outlineVariant = Color(0xFFD8CEC1),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF342E27),
    inverseOnSurface = Color(0xFFF8F1E8),
    inversePrimary = Color(0xFFFF9466),
    surfaceDim = Color(0xFFDCD5CB),
    surfaceBright = Color(0xFFFBF7F1),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF4EEE5),
    surfaceContainer = Color(0xFFEDE5DA),
    surfaceContainerHigh = Color(0xFFE5DCCF),
    surfaceContainerHighest = Color(0xFFDED4C6),
)

/** Ember palette, dark theme. Not the light theme inverted: primary lightens and takes dark on-primary text. */
private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF9466),
    onPrimary = Color(0xFF4A1800),
    primaryContainer = Color(0xFF7A2E08),
    onPrimaryContainer = Color(0xFFFFDBCC),
    secondary = Color(0xFFDFC4A2),
    onSecondary = Color(0xFF3F2B12),
    secondaryContainer = Color(0xFF4A3A22),
    onSecondaryContainer = Color(0xFFF3DFC6),
    tertiary = Color(0xFF7EA6FF),
    onTertiary = Color(0xFF0B1F6B),
    tertiaryContainer = Color(0xFF1E3A8A),
    onTertiaryContainer = Color(0xFFDCE3FF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF410E0B),
    errorContainer = Color(0xFF5C1A14),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF15120E),
    onBackground = Color(0xFFF1EAE0),
    surface = Color(0xFF15120E),
    onSurface = Color(0xFFF1EAE0),
    surfaceVariant = Color(0xFF37302A),
    onSurfaceVariant = Color(0xFFB4A99A),
    outline = Color(0xFF7F7467),
    outlineVariant = Color(0xFF3E362E),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFF1EAE0),
    inverseOnSurface = Color(0xFF342E27),
    inversePrimary = Color(0xFFBC3D0A),
    surfaceDim = Color(0xFF15120E),
    surfaceBright = Color(0xFF3C352E),
    surfaceContainerLowest = Color(0xFF100D0A),
    surfaceContainerLow = Color(0xFF1C1813),
    surfaceContainer = Color(0xFF231E18),
    surfaceContainerHigh = Color(0xFF2C2620),
    surfaceContainerHighest = Color(0xFF37302A),
)

@Composable
fun SnoutTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val materialColorScheme = if (darkTheme) DarkColors else LightColors
    val extendedColorScheme = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalSnoutExtendedColors provides extendedColorScheme) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = SnoutTypography,
            shapes = SnoutMaterialShapes,
            content = content,
        )
    }
}

object SnoutTheme {
    val colors: ColorScheme
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

    val extendedColors: SnoutExtendedColors
        @Composable @ReadOnlyComposable
        get() = LocalSnoutExtendedColors.current

    val typography: Typography
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.typography

    /** The monospace styles; the same in both themes. */
    val code: SnoutCodeTypography = SnoutCodeStyles

    val shapes: Shapes
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.shapes
}
