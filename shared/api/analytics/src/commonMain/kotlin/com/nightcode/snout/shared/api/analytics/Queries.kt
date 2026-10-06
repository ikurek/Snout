package com.nightcode.snout.shared.api.analytics

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/** Web projects fire `$pageview`, mobile projects fire `$screen`; web analytics counts both as visits. */
private val VisitEvents = listOf("\$pageview", "\$screen")

/** One trends series to request: PostHog echoes [name] back as `custom_name` so results can be told apart. */
internal data class TrendSeriesSpec(val name: String, val math: String)

internal fun webOverviewQuery(dateFrom: String): JsonObject = buildJsonObject {
    put("kind", "WebOverviewQuery")
    putDateRange(dateFrom)
    putJsonArray("properties") {}
    putJsonObject("compareFilter") { put("compare", true) }
}

internal fun webStatsTableQuery(dateFrom: String, breakdownBy: String, limit: Int): JsonObject = buildJsonObject {
    put("kind", "WebStatsTableQuery")
    put("breakdownBy", breakdownBy)
    putDateRange(dateFrom)
    putJsonArray("properties") {}
    put("limit", limit)
    putJsonObject("compareFilter") { put("compare", false) }
}

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
): JsonObject = buildJsonObject {
    put("kind", "TrendsQuery")
    putDateRange(dateFrom)
    put("interval", interval)
    put(
        "series",
        buildJsonArray {
            series.forEach { spec ->
                VisitEvents.forEach { event ->
                    add(
                        buildJsonObject {
                            put("kind", "EventsNode")
                            put("event", event)
                            put("name", event)
                            put("custom_name", spec.name)
                            put("math", spec.math)
                        },
                    )
                }
            }
        },
    )
    putJsonObject("trendsFilter") {
        put("display", if (breakdownProperty == null) "ActionsLineGraph" else "ActionsBarValue")
    }
    if (compare) {
        putJsonObject("compareFilter") { put("compare", true) }
    }
    if (breakdownProperty != null) {
        putJsonObject("breakdownFilter") {
            put("breakdown", breakdownProperty)
            put("breakdown_type", "event")
            breakdownLimit?.let { put("breakdown_limit", it) }
        }
    }
}

private fun JsonObjectBuilder.putDateRange(dateFrom: String) {
    putJsonObject("dateRange") {
        put("date_from", dateFrom)
        put("date_to", JsonPrimitive(null as String?))
    }
}
