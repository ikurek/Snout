package com.nightcode.snout.shared.api.analytics

import com.nightcode.snout.shared.api.DateRangeDto
import com.nightcode.snout.shared.api.analytics.model.BreakdownFilterDto
import com.nightcode.snout.shared.api.analytics.model.CompareFilterDto
import com.nightcode.snout.shared.api.analytics.model.EventsNodeDto
import com.nightcode.snout.shared.api.analytics.model.HogQLQueryDto
import com.nightcode.snout.shared.api.analytics.model.TrendsFilterDto
import com.nightcode.snout.shared.api.analytics.model.TrendsQueryDto
import com.nightcode.snout.shared.api.analytics.model.WebOverviewQueryDto
import com.nightcode.snout.shared.api.analytics.model.WebStatsTableQueryDto

/** Web projects fire `$pageview`, mobile projects fire `$screen`; web analytics counts both as visits. */
private val VisitEvents = listOf("\$pageview", "\$screen")

/** One trends series to request: PostHog echoes [name] back as `custom_name` so results can be told apart. */
internal data class TrendSeriesSpec(val name: String, val math: String)

internal fun webOverviewQuery(dateFrom: String) = WebOverviewQueryDto(
    dateRange = DateRangeDto(dateFrom),
    properties = emptyList(),
    compareFilter = CompareFilterDto(compare = true),
)

internal fun webStatsTableQuery(dateFrom: String, breakdownBy: String, limit: Int) = WebStatsTableQueryDto(
    breakdownBy = breakdownBy,
    dateRange = DateRangeDto(dateFrom),
    properties = emptyList(),
    limit = limit,
    compareFilter = CompareFilterDto(compare = false),
)

/**
 * A TrendsQuery over the visit events. With [compare] PostHog adds a second set of results for the preceding
 * period; with [breakdownProperty] it returns one item per property value and `display` switches to a totals
 * table so `aggregated_value` holds unique users over the whole range.
 */
internal fun trendsQuery(
    dateFrom: String,
    interval: String,
    series: List<TrendSeriesSpec>,
    compare: Boolean = false,
    breakdownProperty: String? = null,
    breakdownLimit: Int? = null,
) = TrendsQueryDto(
    dateRange = DateRangeDto(dateFrom),
    interval = interval,
    series = series.flatMap { spec ->
        VisitEvents.map { event ->
            EventsNodeDto(event = event, name = event, customName = spec.name, math = spec.math)
        }
    },
    trendsFilter = TrendsFilterDto(display = if (breakdownProperty == null) "ActionsLineGraph" else "ActionsBarValue"),
    compareFilter = if (compare) CompareFilterDto(compare = true) else null,
    breakdownFilter = breakdownProperty?.let {
        BreakdownFilterDto(breakdown = it, breakdownType = "event", breakdownLimit = breakdownLimit)
    },
)

/**
 * Every revenue source's MRR and revenue to date, how many customers they come from, how many of those pay a
 * subscription now and how many ever paid: PostHog's revenue analytics keeps both per person, whichever source (Stripe,
 * revenue events) they came from. No customers means it isn't set up.
 */
internal val RevenueTotalsQuery = HogQLQueryDto(
    "SELECT sum(mrr), sum(revenue), count(), countIf(mrr > 0), countIf(revenue > 0) FROM persons_revenue_analytics",
)

/** The app versions events carried over the last week, most used first. */
internal val AppVersionsQuery = HogQLQueryDto(
    """
    SELECT properties.${'$'}app_version AS version, count()
    FROM events
    WHERE timestamp > now() - INTERVAL 7 DAY AND version != ''
    GROUP BY version
    ORDER BY count() DESC
    LIMIT 50
    """.trimIndent(),
)

/**
 * Exceptions in the range [dateFrom] names (as a trends query's) and in the same span just before it, then the users
 * who ran into them in each.
 */
internal fun exceptionCountsQuery(dateFrom: String): HogQLQueryDto {
    val (start, previousStart, previousEnd) = window(dateFrom)
    val previous = "timestamp >= $previousStart AND timestamp < $previousEnd"
    return HogQLQueryDto(
        """
        SELECT countIf(timestamp >= $start), countIf($previous),
            uniqIf(person_id, timestamp >= $start), uniqIf(person_id, $previous)
        FROM events
        WHERE event = '${'$'}exception' AND timestamp >= $previousStart
        """.trimIndent(),
    )
}

/**
 * The start of the range [dateFrom] names, and of the span before it with its end: today so far is compared with
 * yesterday up to the same time, the last N days with the N before.
 */
private fun window(dateFrom: String): Triple<String, String, String> = if (dateFrom == "dStart") {
    Triple("toStartOfDay(now())", "toStartOfDay(now()) - INTERVAL 1 DAY", "now() - INTERVAL 1 DAY")
} else {
    val days = requireNotNull(dateFrom.removePrefix("-").removeSuffix("d").toIntOrNull()) { "Not a range: $dateFrom" }
    Triple("now() - INTERVAL $days DAY", "now() - INTERVAL ${days * 2} DAY", "now() - INTERVAL $days DAY")
}
