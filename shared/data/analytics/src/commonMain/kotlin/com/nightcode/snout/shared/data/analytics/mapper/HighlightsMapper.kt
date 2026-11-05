package com.nightcode.snout.shared.data.analytics.mapper

import com.nightcode.snout.shared.api.analytics.TrendSeriesNames
import com.nightcode.snout.shared.api.analytics.model.QueryResponseDto
import com.nightcode.snout.shared.api.analytics.model.TrendResultDto
import com.nightcode.snout.shared.data.common.number
import com.nightcode.snout.shared.data.common.string
import com.nightcode.snout.shared.domain.model.analytics.Highlight
import com.nightcode.snout.shared.domain.model.analytics.HighlightMetric
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKind
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

/** What PostHog reports revenue in when a project never picked a currency. */
private const val DefaultCurrency = "USD"

/**
 * Daily, weekly and monthly active users from a week of daily points: weekly and monthly as of the last point, as the
 * Analytics tab shows them, daily for the day before it, since today's isn't over yet. Each is compared with the same
 * point of the week before.
 */
internal fun QueryResponseDto.toActiveUserHighlights(json: Json): Map<HighlightMetric, Highlight> {
    val items = json.decodeFromJsonElement<List<TrendResultDto>>(results)
    return listOf(
        Triple(HighlightMetric.DailyActiveUsers, TrendSeriesNames.DailyActiveUsers, 2),
        Triple(HighlightMetric.WeeklyActiveUsers, TrendSeriesNames.WeeklyActiveUsers, 1),
        Triple(HighlightMetric.MonthlyActiveUsers, TrendSeriesNames.MonthlyActiveUsers, 1),
    ).associate { (metric, name, fromEnd) ->
        val value = items.named(name).current().sumSeries()?.points?.let { it.getOrNull(it.size - fromEnd) }?.value
        val previous = items.named(name).previous().sumSeries()?.points?.let { it.getOrNull(it.size - fromEnd) }?.value
        val highlight = value?.let { Highlight.Figure(it, OverviewMetricKind.Count, changePercent(it, previous)) }
        metric to (highlight ?: Highlight.None)
    }
}

/** MRR and revenue to date in [currency]; none when no customer brought any, as before revenue analytics is set up. */
internal fun QueryResponseDto.toRevenueHighlights(json: Json, currency: String?): Map<HighlightMetric, Highlight> {
    val row = json.decodeFromJsonElement<List<List<JsonElement>>>(results).firstOrNull().orEmpty()
    val customers = row.getOrNull(2).number() ?: 0.0
    val code = currency?.takeIf { it.isNotBlank() } ?: DefaultCurrency
    fun money(index: Int): Highlight =
        row.getOrNull(index).number()?.takeIf { customers > 0 }?.let { Highlight.Money(it, code) } ?: Highlight.None
    return mapOf(HighlightMetric.Mrr to money(0), HighlightMetric.Revenue to money(1))
}

/** The newest of the versions events carried, by their numbers ("3.10.0" after "3.9.2"), whichever is most used. */
internal fun QueryResponseDto.toLatestAppVersion(json: Json): Highlight =
    json.decodeFromJsonElement<List<List<JsonElement>>>(results)
        .mapNotNull { row -> row.firstOrNull().string()?.takeIf { it.isNotBlank() } }
        .maxWithOrNull(VersionOrder)
        ?.let { Highlight.Version(it) }
        ?: Highlight.None

/** Exceptions in the range, compared with the period before; more is worse. */
internal fun QueryResponseDto.toExceptionsHighlight(json: Json): Highlight {
    val row = json.decodeFromJsonElement<List<List<JsonElement>>>(results).firstOrNull().orEmpty()
    val value = row.getOrNull(0).number() ?: return Highlight.None
    return Highlight.Figure(
        value = value,
        kind = OverviewMetricKind.Count,
        changePercent = changePercent(value, row.getOrNull(1).number()),
        isIncreaseBad = true,
    )
}

private fun changePercent(value: Double, previous: Double?): Double? =
    previous?.takeIf { it > 0 }?.let { (value - it) / it * PercentFactor }

/** Compares versions number by number, so "3.10" is newer than "3.9"; a part without a number counts as zero. */
internal val VersionOrder = Comparator<String> { first, second ->
    val firstParts = first.versionParts()
    val secondParts = second.versionParts()
    (0 until maxOf(firstParts.size, secondParts.size))
        .map { (firstParts.getOrNull(it) ?: 0).compareTo(secondParts.getOrNull(it) ?: 0) }
        .firstOrNull { it != 0 }
        ?: 0
}

private fun String.versionParts(): List<Long> =
    split('.', '-', '+', ' ').map { part -> part.takeWhile { it.isDigit() }.toLongOrNull() ?: 0 }
