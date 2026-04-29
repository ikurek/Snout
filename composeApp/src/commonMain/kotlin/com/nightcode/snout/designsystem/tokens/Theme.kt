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

    secondary = SnoutLightTokens.Primary,
    onSecondary = SnoutLightTokens.OnPrimary,
    secondaryContainer = SnoutLightTokens.PrimaryContainer,
    onSecondaryContainer = SnoutLightTokens.OnPrimaryContainer,

    tertiary = SnoutLightTokens.Info,
    onTertiary = SnoutLightTokens.OnPrimary,

    background = SnoutLightTokens.Surface,
    onBackground = SnoutLightTokens.OnSurface,
    surface = SnoutLightTokens.Surface,
    onSurface = SnoutLightTokens.OnSurface,
    surfaceVariant = SnoutLightTokens.SurfaceVariant,
    onSurfaceVariant = SnoutLightTokens.OnSurfaceVariant,
    surfaceDim = SnoutLightTokens.SurfaceDim,
    surfaceContainerLowest = SnoutLightTokens.Surface,
    surfaceContainerLow = SnoutLightTokens.SurfaceContainerLow,
    surfaceContainer = SnoutLightTokens.SurfaceContainer,
    surfaceContainerHigh = SnoutLightTokens.SurfaceContainerHigh,
    surfaceContainerHighest = SnoutLightTokens.SurfaceContainerHighest,

    outline = SnoutLightTokens.Outline,
    outlineVariant = SnoutLightTokens.OutlineVariant,

    error = SnoutLightTokens.Error,
    onError = SnoutLightTokens.OnError,
    errorContainer = SnoutLightTokens.ErrorContainer,
    onErrorContainer = SnoutLightTokens.OnErrorContainer,
)

private val DarkColors = darkColorScheme(
    primary = SnoutDarkTokens.Primary,
    onPrimary = SnoutDarkTokens.OnPrimary,
    primaryContainer = SnoutDarkTokens.PrimaryContainer,
    onPrimaryContainer = SnoutDarkTokens.OnPrimaryContainer,

    secondary = SnoutDarkTokens.Primary,
    onSecondary = SnoutDarkTokens.OnPrimary,
    secondaryContainer = SnoutDarkTokens.PrimaryContainer,
    onSecondaryContainer = SnoutDarkTokens.OnPrimaryContainer,

    tertiary = SnoutDarkTokens.Info,
    onTertiary = SnoutDarkTokens.OnPrimary,

    background = SnoutDarkTokens.Surface,
    onBackground = SnoutDarkTokens.OnSurface,
    surface = SnoutDarkTokens.Surface,
    onSurface = SnoutDarkTokens.OnSurface,
    surfaceVariant = SnoutDarkTokens.SurfaceVariant,
    onSurfaceVariant = SnoutDarkTokens.OnSurfaceVariant,
    surfaceDim = SnoutDarkTokens.SurfaceDim,
    surfaceContainerLowest = SnoutDarkTokens.SurfaceDim,
    surfaceContainerLow = SnoutDarkTokens.SurfaceContainerLow,
    surfaceContainer = SnoutDarkTokens.SurfaceContainer,
    surfaceContainerHigh = SnoutDarkTokens.SurfaceContainerHigh,
    surfaceContainerHighest = SnoutDarkTokens.SurfaceContainerHighest,

    outline = SnoutDarkTokens.Outline,
    outlineVariant = SnoutDarkTokens.OutlineVariant,

    error = SnoutDarkTokens.Error,
    onError = SnoutDarkTokens.OnError,
    errorContainer = SnoutDarkTokens.ErrorContainer,
    onErrorContainer = SnoutDarkTokens.OnErrorContainer,
)

@Composable
fun SnoutTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val materialColorScheme = if (darkTheme) DarkColors else LightColors
    val extendedColorScheme = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(
        LocalSnoutExtendedColors provides extendedColorScheme,
        LocalSnoutMonoTypography provides SnoutMonoTypography,
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

    val monoTypography: Typography
        @Composable @ReadOnlyComposable
        get() = LocalSnoutMonoTypography.current

    val shapes: Shapes
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.shapes
}
