package com.nightcode.snout.shared.api.analytics

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

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

internal fun pageviewTrendQuery(dateFrom: String, interval: String): JsonObject = buildJsonObject {
    put("kind", "TrendsQuery")
    putDateRange(dateFrom)
    put("interval", interval)
    put(
        "series",
        buildJsonArray {
            add(
                buildJsonObject {
                    put("kind", "EventsNode")
                    put("event", "\$pageview")
                    put("name", "\$pageview")
                    put("math", "dau")
                },
            )
        },
    )
    putJsonObject("trendsFilter") { put("display", "ActionsLineGraph") }
}

private fun kotlinx.serialization.json.JsonObjectBuilder.putDateRange(dateFrom: String) {
    putJsonObject("dateRange") {
        put("date_from", dateFrom)
        put("date_to", JsonPrimitive(null as String?))
    }
}
