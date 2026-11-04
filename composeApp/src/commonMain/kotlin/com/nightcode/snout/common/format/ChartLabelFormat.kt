package com.nightcode.snout.common.format

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

private const val DateLength = 10
private const val MinuteEnd = 19
private const val MonthAbbreviation = 3
private const val TwoDigits = 2

/**
 * PostHog returns period labels as ISO dates or timestamps. Daily points become "5 Oct",
 * hourly points become "14:00"; anything else is shown as received.
 */
internal fun String.formatChartLabel(): String {
    // parseOrNull, not parse in runCatching: a chart has a label per point, and exceptions are slow on iOS.
    val date = if (length <= DateLength) LocalDate.Formats.ISO.parseOrNull(this) else null
    val dateTime = if (date == null) LocalDateTime.Formats.ISO.parseOrNull(replace(' ', 'T').take(MinuteEnd)) else null
    return when {
        date != null -> "${date.day} ${date.month.name.take(MonthAbbreviation).titleCase()}"
        dateTime != null -> "${dateTime.hour.twoDigits()}:${dateTime.minute.twoDigits()}"
        else -> this
    }
}

private fun String.titleCase(): String = lowercase().replaceFirstChar { it.uppercase() }

private fun Int.twoDigits(): String = toString().padStart(TwoDigits, '0')
