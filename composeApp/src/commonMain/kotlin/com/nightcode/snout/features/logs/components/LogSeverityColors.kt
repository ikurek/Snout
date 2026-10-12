package com.nightcode.snout.features.logs.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.shared.domain.model.logs.LogSeverity

/**
 * Colour of a level in volume charts and bars. Errors and warnings take the same tones as their tags; the routine
 * levels stay neutral so trouble stands out.
 */
@Composable
internal fun LogSeverity.volumeColor(): Color = when (this) {
    LogSeverity.Error, LogSeverity.Fatal -> SnoutTheme.colors.error
    LogSeverity.Warn -> SnoutTheme.colors.tertiary
    LogSeverity.Info -> SnoutTheme.colors.outline
    LogSeverity.Trace, LogSeverity.Debug, LogSeverity.Unknown -> SnoutTheme.colors.outlineVariant
}
