package com.nightcode.snout.designsystem.tokens

import androidx.compose.ui.graphics.Color

/** Ember palette, light theme. Every value is a Material 3 role so the whole set drops into [SnoutTheme]. */
internal object SnoutLightTokens {
    val Primary = Color(0xFFBC3D0A)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFFFDBCC)
    val OnPrimaryContainer = Color(0xFF5A1E00)

    val Secondary = Color(0xFF705B3B)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFF3DFC6)
    val OnSecondaryContainer = Color(0xFF3F2B12)

    val Tertiary = Color(0xFF1D4ED8)
    val OnTertiary = Color(0xFFFFFFFF)
    val TertiaryContainer = Color(0xFFDCE3FF)
    val OnTertiaryContainer = Color(0xFF0B1F6B)

    val Error = Color(0xFFB3261E)
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFF9DEDC)
    val OnErrorContainer = Color(0xFF410E0B)

    val Background = Color(0xFFFBF7F1)
    val OnBackground = Color(0xFF1F1A14)
    val Surface = Color(0xFFFBF7F1)
    val OnSurface = Color(0xFF1F1A14)
    val SurfaceVariant = Color(0xFFDED4C6)
    val OnSurfaceVariant = Color(0xFF5E554B)
    val Outline = Color(0xFF857A6D)
    val OutlineVariant = Color(0xFFD8CEC1)
    val Scrim = Color(0xFF000000)
    val InverseSurface = Color(0xFF342E27)
    val InverseOnSurface = Color(0xFFF8F1E8)
    val InversePrimary = Color(0xFFFF9466)
    val SurfaceDim = Color(0xFFDCD5CB)
    val SurfaceBright = Color(0xFFFBF7F1)
    val SurfaceContainerLowest = Color(0xFFFFFFFF)
    val SurfaceContainerLow = Color(0xFFF4EEE5)
    val SurfaceContainer = Color(0xFFEDE5DA)
    val SurfaceContainerHigh = Color(0xFFE5DCCF)
    val SurfaceContainerHighest = Color(0xFFDED4C6)

    // Extended roles Material 3 does not define
    val Success = Color(0xFF276B43)
    val OnSuccess = Color(0xFFFFFFFF)
    val SuccessContainer = Color(0xFFD4F0DF)
    val OnSuccessContainer = Color(0xFF0F3D24)
    val Chart = listOf(
        Color(0xFFBC3D0A),
        Color(0xFF1D4ED8),
        Color(0xFF0F766E),
        Color(0xFF7E22CE),
        Color(0xFFB45309),
    )
}

/** Ember palette, dark theme. Not the light theme inverted: primary lightens and takes dark on-primary text. */
internal object SnoutDarkTokens {
    val Primary = Color(0xFFFF9466)
    val OnPrimary = Color(0xFF4A1800)
    val PrimaryContainer = Color(0xFF7A2E08)
    val OnPrimaryContainer = Color(0xFFFFDBCC)

    val Secondary = Color(0xFFDFC4A2)
    val OnSecondary = Color(0xFF3F2B12)
    val SecondaryContainer = Color(0xFF4A3A22)
    val OnSecondaryContainer = Color(0xFFF3DFC6)

    val Tertiary = Color(0xFF7EA6FF)
    val OnTertiary = Color(0xFF0B1F6B)
    val TertiaryContainer = Color(0xFF1E3A8A)
    val OnTertiaryContainer = Color(0xFFDCE3FF)

    val Error = Color(0xFFFFB4AB)
    val OnError = Color(0xFF410E0B)
    val ErrorContainer = Color(0xFF5C1A14)
    val OnErrorContainer = Color(0xFFFFDAD6)

    val Background = Color(0xFF15120E)
    val OnBackground = Color(0xFFF1EAE0)
    val Surface = Color(0xFF15120E)
    val OnSurface = Color(0xFFF1EAE0)
    val SurfaceVariant = Color(0xFF37302A)
    val OnSurfaceVariant = Color(0xFFB4A99A)
    val Outline = Color(0xFF7F7467)
    val OutlineVariant = Color(0xFF3E362E)
    val Scrim = Color(0xFF000000)
    val InverseSurface = Color(0xFFF1EAE0)
    val InverseOnSurface = Color(0xFF342E27)
    val InversePrimary = Color(0xFFBC3D0A)
    val SurfaceDim = Color(0xFF15120E)
    val SurfaceBright = Color(0xFF3C352E)
    val SurfaceContainerLowest = Color(0xFF100D0A)
    val SurfaceContainerLow = Color(0xFF1C1813)
    val SurfaceContainer = Color(0xFF231E18)
    val SurfaceContainerHigh = Color(0xFF2C2620)
    val SurfaceContainerHighest = Color(0xFF37302A)

    // Extended roles Material 3 does not define
    val Success = Color(0xFF7FD79F)
    val OnSuccess = Color(0xFF0F3D24)
    val SuccessContainer = Color(0xFF1E4A30)
    val OnSuccessContainer = Color(0xFFCFF3DB)
    val Chart = listOf(
        Color(0xFFFF9466),
        Color(0xFF7EA6FF),
        Color(0xFF4FD1C5),
        Color(0xFFC79BFF),
        Color(0xFFF2C14E),
    )
}
