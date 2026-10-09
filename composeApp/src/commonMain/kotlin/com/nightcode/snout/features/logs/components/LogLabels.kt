package com.nightcode.snout.features.logs.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.nightcode.snout.designsystem.atoms.StatusTone
import com.nightcode.snout.designsystem.atoms.Tag
import com.nightcode.snout.designsystem.molecules.Placeholder
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.shared.domain.model.logs.LogSeverity
import com.nightcode.snout.shared.domain.model.logs.LogSeverityFilter
import com.nightcode.snout.shared.domain.model.logs.LogsRange
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_info_outlined_24dp
import snout.composeapp.generated.resources.logs_level_debug
import snout.composeapp.generated.resources.logs_level_errors
import snout.composeapp.generated.resources.logs_level_info
import snout.composeapp.generated.resources.logs_level_warnings
import snout.composeapp.generated.resources.logs_range_last_24_hours
import snout.composeapp.generated.resources.logs_range_last_7_days
import snout.composeapp.generated.resources.logs_range_last_hour
import snout.composeapp.generated.resources.logs_severity_all
import snout.composeapp.generated.resources.logs_severity_errors
import snout.composeapp.generated.resources.logs_severity_warnings
import snout.composeapp.generated.resources.logs_unavailable_subtitle
import snout.composeapp.generated.resources.logs_unavailable_title
import snout.composeapp.generated.resources.placeholder_generic_error_retry

internal val LogsRange.label: StringResource
    get() = when (this) {
        LogsRange.LastHour -> Res.string.logs_range_last_hour
        LogsRange.Last24Hours -> Res.string.logs_range_last_24_hours
        LogsRange.Last7Days -> Res.string.logs_range_last_7_days
    }

internal val LogSeverityFilter.label: StringResource
    get() = when (this) {
        LogSeverityFilter.All -> Res.string.logs_severity_all
        LogSeverityFilter.WarningsAndErrors -> Res.string.logs_severity_warnings
        LogSeverityFilter.Errors -> Res.string.logs_severity_errors
    }

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

/** Plural name of a level group, for chart legends. */
internal val LogSeverity.volumeLabel: StringResource
    get() = when (this) {
        LogSeverity.Error, LogSeverity.Fatal -> Res.string.logs_level_errors
        LogSeverity.Warn -> Res.string.logs_level_warnings
        LogSeverity.Info -> Res.string.logs_level_info
        LogSeverity.Trace, LogSeverity.Debug, LogSeverity.Unknown -> Res.string.logs_level_debug
    }

/** Upper-case level the way log viewers print it; only warnings and errors take a colour. */
@Composable
internal fun LogSeverityTag(severity: LogSeverity, modifier: Modifier = Modifier) {
    val tone = when (severity) {
        LogSeverity.Error, LogSeverity.Fatal -> StatusTone.Attention
        LogSeverity.Warn -> StatusTone.Info
        LogSeverity.Trace, LogSeverity.Debug, LogSeverity.Info, LogSeverity.Unknown -> StatusTone.Neutral
    }
    val text = if (severity == LogSeverity.Unknown) "?" else severity.name.uppercase()
    Tag(text = text, tone = tone, modifier = modifier)
}

/** PostHog answers 404 for the Logs API on instances that predate it. */
@Composable
internal fun LogsUnavailablePlaceholder(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Placeholder(
        icon = painterResource(Res.drawable.ic_info_outlined_24dp),
        title = stringResource(Res.string.logs_unavailable_title),
        message = stringResource(Res.string.logs_unavailable_subtitle),
        actionLabel = stringResource(Res.string.placeholder_generic_error_retry),
        onAction = onRetry,
        modifier = modifier,
    )
}
