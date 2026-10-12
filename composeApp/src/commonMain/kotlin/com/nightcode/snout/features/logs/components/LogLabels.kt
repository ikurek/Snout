package com.nightcode.snout.features.logs.components

import com.nightcode.snout.shared.domain.model.logs.LogSeverity
import com.nightcode.snout.shared.domain.model.logs.LogSeverityFilter
import com.nightcode.snout.shared.domain.model.logs.LogsRange
import org.jetbrains.compose.resources.StringResource
import snout.composeapp.generated.resources.Res
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

/** Plural name of a level group, for chart legends. */
internal val LogSeverity.volumeLabel: StringResource
    get() = when (this) {
        LogSeverity.Error, LogSeverity.Fatal -> Res.string.logs_level_errors
        LogSeverity.Warn -> Res.string.logs_level_warnings
        LogSeverity.Info -> Res.string.logs_level_info
        LogSeverity.Trace, LogSeverity.Debug, LogSeverity.Unknown -> Res.string.logs_level_debug
    }
