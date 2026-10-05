package com.nightcode.snout.common.format

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.month_april
import snout.composeapp.generated.resources.month_august
import snout.composeapp.generated.resources.month_december
import snout.composeapp.generated.resources.month_february
import snout.composeapp.generated.resources.month_january
import snout.composeapp.generated.resources.month_july
import snout.composeapp.generated.resources.month_june
import snout.composeapp.generated.resources.month_march
import snout.composeapp.generated.resources.month_may
import snout.composeapp.generated.resources.month_november
import snout.composeapp.generated.resources.month_october
import snout.composeapp.generated.resources.month_september
import snout.composeapp.generated.resources.relative_time_days_ago
import snout.composeapp.generated.resources.relative_time_hours_ago
import snout.composeapp.generated.resources.relative_time_just_now
import snout.composeapp.generated.resources.relative_time_minutes_ago
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

private const val ShortMonthLength = 3
private const val RelativeDaysLimit = 7

@Composable
private fun localizedMonthNames(): MonthNames = MonthNames(
    january = stringResource(Res.string.month_january),
    february = stringResource(Res.string.month_february),
    march = stringResource(Res.string.month_march),
    april = stringResource(Res.string.month_april),
    may = stringResource(Res.string.month_may),
    june = stringResource(Res.string.month_june),
    july = stringResource(Res.string.month_july),
    august = stringResource(Res.string.month_august),
    september = stringResource(Res.string.month_september),
    october = stringResource(Res.string.month_october),
    november = stringResource(Res.string.month_november),
    december = stringResource(Res.string.month_december),
)

@Composable
private fun shortMonthNames(): MonthNames = MonthNames(localizedMonthNames().names.map { it.take(ShortMonthLength) })

private fun Instant.local(): LocalDateTime = toLocalDateTime(TimeZone.currentSystemDefault())

/** "January 2024" */
@Composable
internal fun Instant.formatMonthYear(): String {
    val monthNames = localizedMonthNames()
    return LocalDateTime.Format {
        monthName(monthNames)
        char(' ')
        year()
    }.format(local())
}

/** "5 Oct 2026" */
@Composable
internal fun Instant.formatShortDate(): String {
    val monthNames = shortMonthNames()
    return LocalDateTime.Format {
        day(Padding.NONE)
        char(' ')
        monthName(monthNames)
        char(' ')
        year()
    }.format(local())
}

/** "5 Oct 2026, 14:05" */
@Composable
internal fun Instant.formatDateTime(): String {
    val monthNames = shortMonthNames()
    return LocalDateTime.Format {
        day(Padding.NONE)
        char(' ')
        monthName(monthNames)
        char(' ')
        year()
        chars(", ")
        hour()
        char(':')
        minute()
    }.format(local())
}

/** "just now", "5 min ago", "3 h ago", "2 d ago", otherwise a short date. */
@Composable
internal fun Instant.formatRelative(now: Instant = Clock.System.now()): String {
    val elapsed = now - this
    return when {
        elapsed < 1.minutes -> stringResource(Res.string.relative_time_just_now)
        elapsed < 1.hours -> stringResource(Res.string.relative_time_minutes_ago, elapsed.inWholeMinutes)
        elapsed < 1.days -> stringResource(Res.string.relative_time_hours_ago, elapsed.inWholeHours)
        elapsed < RelativeDaysLimit.days -> stringResource(Res.string.relative_time_days_ago, elapsed.inWholeDays)
        else -> formatShortDate()
    }
}
