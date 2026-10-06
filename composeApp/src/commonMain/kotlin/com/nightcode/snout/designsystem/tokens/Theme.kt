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

private val LightColors = lightColorScheme(
    primary = SnoutLightTokens.Primary,
    onPrimary = SnoutLightTokens.OnPrimary,
    primaryContainer = SnoutLightTokens.PrimaryContainer,
    onPrimaryContainer = SnoutLightTokens.OnPrimaryContainer,
    secondary = SnoutLightTokens.Secondary,
    onSecondary = SnoutLightTokens.OnSecondary,
    secondaryContainer = SnoutLightTokens.SecondaryContainer,
    onSecondaryContainer = SnoutLightTokens.OnSecondaryContainer,
    tertiary = SnoutLightTokens.Tertiary,
    onTertiary = SnoutLightTokens.OnTertiary,
    tertiaryContainer = SnoutLightTokens.TertiaryContainer,
    onTertiaryContainer = SnoutLightTokens.OnTertiaryContainer,
    error = SnoutLightTokens.Error,
    onError = SnoutLightTokens.OnError,
    errorContainer = SnoutLightTokens.ErrorContainer,
    onErrorContainer = SnoutLightTokens.OnErrorContainer,
    background = SnoutLightTokens.Background,
    onBackground = SnoutLightTokens.OnBackground,
    surface = SnoutLightTokens.Surface,
    onSurface = SnoutLightTokens.OnSurface,
    surfaceVariant = SnoutLightTokens.SurfaceVariant,
    onSurfaceVariant = SnoutLightTokens.OnSurfaceVariant,
    outline = SnoutLightTokens.Outline,
    outlineVariant = SnoutLightTokens.OutlineVariant,
    scrim = SnoutLightTokens.Scrim,
    inverseSurface = SnoutLightTokens.InverseSurface,
    inverseOnSurface = SnoutLightTokens.InverseOnSurface,
    inversePrimary = SnoutLightTokens.InversePrimary,
    surfaceDim = SnoutLightTokens.SurfaceDim,
    surfaceBright = SnoutLightTokens.SurfaceBright,
    surfaceContainerLowest = SnoutLightTokens.SurfaceContainerLowest,
    surfaceContainerLow = SnoutLightTokens.SurfaceContainerLow,
    surfaceContainer = SnoutLightTokens.SurfaceContainer,
    surfaceContainerHigh = SnoutLightTokens.SurfaceContainerHigh,
    surfaceContainerHighest = SnoutLightTokens.SurfaceContainerHighest,
)

private val DarkColors = darkColorScheme(
    primary = SnoutDarkTokens.Primary,
    onPrimary = SnoutDarkTokens.OnPrimary,
    primaryContainer = SnoutDarkTokens.PrimaryContainer,
    onPrimaryContainer = SnoutDarkTokens.OnPrimaryContainer,
    secondary = SnoutDarkTokens.Secondary,
    onSecondary = SnoutDarkTokens.OnSecondary,
    secondaryContainer = SnoutDarkTokens.SecondaryContainer,
    onSecondaryContainer = SnoutDarkTokens.OnSecondaryContainer,
    tertiary = SnoutDarkTokens.Tertiary,
    onTertiary = SnoutDarkTokens.OnTertiary,
    tertiaryContainer = SnoutDarkTokens.TertiaryContainer,
    onTertiaryContainer = SnoutDarkTokens.OnTertiaryContainer,
    error = SnoutDarkTokens.Error,
    onError = SnoutDarkTokens.OnError,
    errorContainer = SnoutDarkTokens.ErrorContainer,
    onErrorContainer = SnoutDarkTokens.OnErrorContainer,
    background = SnoutDarkTokens.Background,
    onBackground = SnoutDarkTokens.OnBackground,
    surface = SnoutDarkTokens.Surface,
    onSurface = SnoutDarkTokens.OnSurface,
    surfaceVariant = SnoutDarkTokens.SurfaceVariant,
    onSurfaceVariant = SnoutDarkTokens.OnSurfaceVariant,
    outline = SnoutDarkTokens.Outline,
    outlineVariant = SnoutDarkTokens.OutlineVariant,
    scrim = SnoutDarkTokens.Scrim,
    inverseSurface = SnoutDarkTokens.InverseSurface,
    inverseOnSurface = SnoutDarkTokens.InverseOnSurface,
    inversePrimary = SnoutDarkTokens.InversePrimary,
    surfaceDim = SnoutDarkTokens.SurfaceDim,
    surfaceBright = SnoutDarkTokens.SurfaceBright,
    surfaceContainerLowest = SnoutDarkTokens.SurfaceContainerLowest,
    surfaceContainerLow = SnoutDarkTokens.SurfaceContainerLow,
    surfaceContainer = SnoutDarkTokens.SurfaceContainer,
    surfaceContainerHigh = SnoutDarkTokens.SurfaceContainerHigh,
    surfaceContainerHighest = SnoutDarkTokens.SurfaceContainerHighest,
)

@Composable
fun SnoutTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val materialColorScheme = if (darkTheme) DarkColors else LightColors
    val extendedColorScheme = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(
        LocalSnoutExtendedColors provides extendedColorScheme,
        LocalSnoutCodeTypography provides SnoutCodeStyles,
    ) {
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

    val code: SnoutCodeTypography
        @Composable @ReadOnlyComposable
        get() = LocalSnoutCodeTypography.current

    val shapes: Shapes
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.shapes
}
