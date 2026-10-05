package com.nightcode.snout.common.format

import kotlin.math.abs
import kotlin.math.roundToLong

private const val Thousand = 1_000.0
private const val Million = 1_000_000.0
private const val Billion = 1_000_000_000.0
private const val Hundred = 100.0
private const val Ten = 10.0

/** 1234 -> "1.2k", 12 -> "12", 0.42 -> "0.42". Locale independent on purpose: dashboards read the same everywhere. */
internal fun Double.formatCompact(): String {
    val magnitude = abs(this)
    return when {
        magnitude >= Billion -> "${(this / Billion).formatDecimals(1)}B"
        magnitude >= Million -> "${(this / Million).formatDecimals(1)}M"
        magnitude >= Thousand -> "${(this / Thousand).formatDecimals(1)}k"
        magnitude >= Hundred -> formatDecimals(0)
        magnitude >= Ten -> formatDecimals(1)
        else -> formatDecimals(2)
    }
}

internal fun Double.formatDecimals(digits: Int): String {
    var factor = 1.0
    repeat(digits) { factor *= Ten }
    val rounded = (this * factor).roundToLong()
    val integerPart = rounded / factor.toLong()
    val fraction = abs(rounded % factor.toLong())
    if (digits == 0) return integerPart.toString()
    val fractionText = fraction.toString().padStart(digits, '0').trimEnd('0')
    val sign = if (rounded < 0 && integerPart == 0L) "-" else ""
    return if (fractionText.isEmpty()) "$sign$integerPart" else "$sign$integerPart.$fractionText"
}

/** 0.1234 -> "12.3%" when [isFraction], 12.34 -> "12.3%" otherwise. */
internal fun Double.formatPercent(isFraction: Boolean = false): String {
    val percent = if (isFraction) this * 100 else this
    return "${percent.formatDecimals(1)}%"
}

/** 83 -> "1m 23s", 3725 -> "1h 2m". */
internal fun Double.formatDurationSeconds(): String {
    val total = roundToLong()
    val hours = total / 3600
    val minutes = total % 3600 / 60
    val seconds = total % 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}

internal fun Double.formatSigned(): String = if (this > 0) "+${formatDecimals(1)}" else formatDecimals(1)
