package com.nightcode.snout.shared.data.analytics.mapper

import com.nightcode.snout.shared.api.analytics.TrendSeriesNames
import com.nightcode.snout.shared.api.analytics.model.QueryResponseDto
import com.nightcode.snout.shared.api.analytics.model.TrendResultDto
import com.nightcode.snout.shared.data.common.number
import com.nightcode.snout.shared.data.common.string
import com.nightcode.snout.shared.domain.model.analytics.Highlight
import com.nightcode.snout.shared.domain.model.analytics.HighlightMetric
import com.nightcode.snout.shared.domain.model.analytics.HighlightSource
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKind
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

/** What PostHog reports revenue in when a project never picked a currency. */
private const val DefaultCurrency = "USD"

/**
 * Daily, weekly and monthly active users from a week of daily points: weekly and monthly as of the last point, as the
 * Analytics tab shows them, daily for the day before it, since today's isn't over yet. Each is compared with the same
 * point of the week before. Stickiness is that day's daily users over the monthly users as of it.
 */
internal fun QueryResponseDto.toActiveUserHighlights(json: Json): Map<HighlightMetric, Highlight> {
    val items = json.decodeFromJsonElement<List<TrendResultDto>>(results)
    fun point(name: String, fromEnd: Int, previous: Boolean): Double? {
        val series = items.named(name).let { if (previous) it.previous() else it.current() }.sumSeries()
        return series?.points?.let { it.getOrNull(it.size - fromEnd) }?.value
    }
    val counts = listOf(
        Triple(HighlightMetric.DailyActiveUsers, TrendSeriesNames.DailyActiveUsers, 2),
        Triple(HighlightMetric.WeeklyActiveUsers, TrendSeriesNames.WeeklyActiveUsers, 1),
        Triple(HighlightMetric.MonthlyActiveUsers, TrendSeriesNames.MonthlyActiveUsers, 1),
    ).associate { (metric, name, fromEnd) ->
        val value = point(name, fromEnd, previous = false)
        val highlight = value?.let {
            Highlight.Figure(it, OverviewMetricKind.Count, changePercent(it, point(name, fromEnd, previous = true)))
        }
        metric to (highlight ?: Highlight.None)
    }

    // Both of the last whole day, as daily users are.
    fun stickiness(previous: Boolean): Double? {
        val daily = point(TrendSeriesNames.DailyActiveUsers, 2, previous)
        val monthly = point(TrendSeriesNames.MonthlyActiveUsers, 2, previous)?.takeIf { it > 0 }
        return if (daily == null || monthly == null) null else (daily / monthly).coerceAtMost(1.0)
    }
    val stickiness = stickiness(previous = false)?.let {
        Highlight.Figure(it, OverviewMetricKind.Percentage, changePercent(it, stickiness(previous = true)))
    }
    return counts + (HighlightMetric.Stickiness to (stickiness ?: Highlight.None))
}

/**
 * MRR and revenue to date in [currency], and the customers paying a subscription and who ever paid; none when no
 * customer brought any, as before revenue analytics is set up. When customers paid but none pays a subscription,
 * PostHog has no MRR to give, which isn't the same as MRR of nothing: it counts it only from subscriptions.
 */
internal fun QueryResponseDto.toRevenueHighlights(json: Json, currency: String?): Map<HighlightMetric, Highlight> {
    val row = json.decodeFromJsonElement<List<List<JsonElement>>>(results).firstOrNull().orEmpty()
    val customers = row.getOrNull(RevenueColumn.Customers.ordinal).number() ?: 0.0
    if (customers <= 0) return RevenueMetrics.associateWith { Highlight.None }
    val code = currency?.takeIf { it.isNotBlank() } ?: DefaultCurrency
    fun number(column: RevenueColumn): Double? = row.getOrNull(column.ordinal).number()
    fun money(column: RevenueColumn): Highlight = number(column)?.let { Highlight.Money(it, code) } ?: Highlight.None
    fun count(value: Double?): Highlight =
        value?.let { Highlight.Figure(it, OverviewMetricKind.Count, changePercent = null) } ?: Highlight.None
    val subscribers = number(RevenueColumn.Subscribers) ?: 0.0
    return mapOf(
        HighlightMetric.Mrr to if (subscribers > 0) money(RevenueColumn.Mrr) else Highlight.NoSubscriptions,
        HighlightMetric.Revenue to money(RevenueColumn.Revenue),
        HighlightMetric.Subscribers to if (subscribers > 0) count(subscribers) else Highlight.NoSubscriptions,
        HighlightMetric.PayingCustomers to count(number(RevenueColumn.PayingCustomers)),
    )
}

/** The columns of `RevenueTotalsQuery`, in its order. */
private enum class RevenueColumn { Mrr, Revenue, Customers, Subscribers, PayingCustomers }

private val RevenueMetrics = HighlightMetric.entries.filter { it.source == HighlightSource.Revenue }

/** The newest of the versions events carried, by their numbers ("3.10.0" after "3.9.2"), whichever is most used. */
internal fun QueryResponseDto.toLatestAppVersion(json: Json): Highlight =
    json.decodeFromJsonElement<List<List<JsonElement>>>(results)
        .mapNotNull { row -> row.firstOrNull().string()?.takeIf { it.isNotBlank() } }
        .maxWithOrNull(VersionOrder)
        ?.let { Highlight.Version(it) }
        ?: Highlight.None

/** Exceptions in the range and the users who ran into them, each compared with the period before; more is worse. */
internal fun QueryResponseDto.toExceptionHighlights(json: Json): Map<HighlightMetric, Highlight> {
    val row = json.decodeFromJsonElement<List<List<JsonElement>>>(results).firstOrNull().orEmpty()
    fun figure(index: Int): Highlight {
        val value = row.getOrNull(index).number() ?: return Highlight.None
        return Highlight.Figure(
            value = value,
            kind = OverviewMetricKind.Count,
            changePercent = changePercent(value, row.getOrNull(index + 1).number()),
            isIncreaseBad = true,
        )
    }
    return mapOf(HighlightMetric.Exceptions to figure(0), HighlightMetric.UsersWithErrors to figure(2))
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
