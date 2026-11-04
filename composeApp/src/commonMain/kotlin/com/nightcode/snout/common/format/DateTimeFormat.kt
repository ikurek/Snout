package com.nightcode.snout.common.format

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.minus
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

/** Overridable "now" so previews and screenshot tests render stable relative times. */
val LocalNow = staticCompositionLocalOf<Instant?> { null }

private const val ShortMonthLength = 3
private const val RelativeDaysLimit = 7

/**
 * Every format the app shows dates in, built once for the localized month names. Building a format and reading
 * twelve string resources per call would otherwise run for every list row and chart label as it composes.
 */
private class DateFormats(monthNames: MonthNames) {
    private val shortMonthNames = MonthNames(monthNames.names.map { it.take(ShortMonthLength) })

    val monthYear: DateTimeFormat<LocalDateTime> = LocalDateTime.Format {
        monthName(monthNames)
        char(' ')
        year()
    }

    val shortDate: DateTimeFormat<LocalDate> = LocalDate.Format {
        day(Padding.NONE)
        char(' ')
        monthName(shortMonthNames)
        char(' ')
        year()
    }

    val dateTime: DateTimeFormat<LocalDateTime> = LocalDateTime.Format {
        date(shortDate)
        chars(", ")
        hour()
        char(':')
        minute()
    }

    val dayMonth: DateTimeFormat<LocalDate> = LocalDate.Format {
        day(Padding.NONE)
        char(' ')
        monthName(shortMonthNames)
    }

    val time: DateTimeFormat<LocalDateTime> = LocalDateTime.Format {
        hour()
        char(':')
        minute()
    }

    val dayMonthTime: DateTimeFormat<LocalDateTime> = LocalDateTime.Format {
        date(dayMonth)
        chars(", ")
        hour()
        char(':')
        minute()
    }

    val timeWithSeconds: DateTimeFormat<LocalDateTime> = LocalDateTime.Format {
        hour()
        char(':')
        minute()
        char(':')
        second()
    }

    val dayMonthTimeWithSeconds: DateTimeFormat<LocalDateTime> = LocalDateTime.Format {
        date(dayMonth)
        chars(", ")
        hour()
        char(':')
        minute()
        char(':')
        second()
    }
}

private val LocalDateFormats = staticCompositionLocalOf<DateFormats?> { null }

/** Builds the date formats once for [content], and again only when the month names (the locale) change. */
@Composable
internal fun ProvideDateFormats(content: @Composable () -> Unit) {
    val monthNames = localizedMonthNames()
    val formats = remember(monthNames.names) { DateFormats(monthNames) }
    CompositionLocalProvider(LocalDateFormats provides formats, content = content)
}

/** The provided formats; previews and screenshot tests outside [ProvideDateFormats] build their own. */
@Composable
private fun dateFormats(): DateFormats = LocalDateFormats.current ?: run {
    val monthNames = localizedMonthNames()
    remember(monthNames.names) { DateFormats(monthNames) }
}

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

private fun Instant.local(): LocalDateTime = toLocalDateTime(TimeZone.currentSystemDefault())

/** "January 2024" */
@Composable
internal fun Instant.formatMonthYear(): String = dateFormats().monthYear.format(local())

/** "5 Oct 2026" */
@Composable
internal fun Instant.formatShortDate(): String = dateFormats().shortDate.format(local().date)

/** "5 Oct 2026" for a calendar date, which has no time zone to convert. */
@Composable
internal fun LocalDate.formatShortDate(): String = dateFormats().shortDate.format(this)

/** "5 Oct 2026, 14:05" */
@Composable
internal fun Instant.formatDateTime(): String = dateFormats().dateTime.format(local())

/** "just now", "5 min ago", "3 h ago", "2 d ago", otherwise a short date. */
@Composable
internal fun Instant.formatRelative(): String {
    val elapsed = (LocalNow.current ?: Clock.System.now()) - this
    return when {
        elapsed < 1.minutes -> stringResource(Res.string.relative_time_just_now)
        elapsed < 1.hours -> stringResource(Res.string.relative_time_minutes_ago, elapsed.inWholeMinutes)
        elapsed < 1.days -> stringResource(Res.string.relative_time_hours_ago, elapsed.inWholeHours)
        elapsed < RelativeDaysLimit.days -> stringResource(Res.string.relative_time_days_ago, elapsed.inWholeDays)
        else -> formatShortDate()
    }
}

/** "5 Oct" for each of the [count] days ending today, oldest first: axis labels for per-day buckets. */
@Composable
internal fun lastDaysLabels(count: Int): List<String> {
    val format = dateFormats().dayMonth
    val today = (LocalNow.current ?: Clock.System.now()).toLocalDateTime(TimeZone.currentSystemDefault()).date
    return remember(format, today, count) {
        (count - 1 downTo 0).map { format.format(today.minus(it, DateTimeUnit.DAY)) }
    }
}

/** "11:58:42" for today, "5 Oct, 11:58:42" otherwise: log lines need seconds and rarely the year. */
@Composable
internal fun Instant.formatLogTime(): String {
    val formats = dateFormats()
    val zone = TimeZone.currentSystemDefault()
    val today = (LocalNow.current ?: Clock.System.now()).toLocalDateTime(zone).date
    val local = toLocalDateTime(zone)
    return (if (local.date != today) formats.dayMonthTimeWithSeconds else formats.timeWithSeconds).format(local)
}

/** "14:00", or "3 Oct, 14:00" with [includeDate]: axis labels for time buckets within a day or across several. */
@Composable
internal fun Instant.formatChartTime(includeDate: Boolean): String {
    val formats = dateFormats()
    return (if (includeDate) formats.dayMonthTime else formats.time).format(local())
}
