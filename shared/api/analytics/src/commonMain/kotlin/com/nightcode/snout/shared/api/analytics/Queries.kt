package com.nightcode.snout.shared.api.analytics

import com.nightcode.snout.shared.api.DateRangeDto
import com.nightcode.snout.shared.api.analytics.model.BreakdownFilterDto
import com.nightcode.snout.shared.api.analytics.model.CompareFilterDto
import com.nightcode.snout.shared.api.analytics.model.EventsNodeDto
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
