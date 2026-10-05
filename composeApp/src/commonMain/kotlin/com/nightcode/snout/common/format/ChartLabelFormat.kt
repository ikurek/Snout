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
    val date = runCatching { LocalDate.parse(take(DateLength)) }.getOrNull()
    val dateTime = runCatching { LocalDateTime.parse(replace(' ', 'T').take(MinuteEnd)) }.getOrNull()
    return when {
        date != null && length <= DateLength -> "${date.day} ${date.month.name.take(MonthAbbreviation).titleCase()}"
        dateTime != null -> "${dateTime.hour.twoDigits()}:${dateTime.minute.twoDigits()}"
        else -> this
    }
}

private fun String.titleCase(): String = lowercase().replaceFirstChar { it.uppercase() }

private fun Int.twoDigits(): String = toString().padStart(TwoDigits, '0')
