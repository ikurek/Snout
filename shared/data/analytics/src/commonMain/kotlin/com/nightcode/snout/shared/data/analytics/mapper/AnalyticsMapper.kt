package com.nightcode.snout.shared.data.analytics.mapper

import com.nightcode.snout.shared.api.analytics.model.QueryResponseDto
import com.nightcode.snout.shared.api.analytics.model.TrendResultDto
import com.nightcode.snout.shared.api.analytics.model.WebOverviewItemDto
import com.nightcode.snout.shared.domain.model.analytics.BreakdownRow
import com.nightcode.snout.shared.domain.model.analytics.BreakdownTable
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetric
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKey
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKind
import com.nightcode.snout.shared.domain.model.analytics.TimeSeries
import com.nightcode.snout.shared.domain.model.analytics.TimeSeriesPoint
import com.nightcode.snout.shared.domain.model.analytics.WebBreakdown
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray

private const val BreakdownColumn = "context.columns.breakdown_value"
private const val VisitorsColumn = "context.columns.visitors"
private const val ViewsColumn = "context.columns.views"

internal fun QueryResponseDto.toOverviewMetrics(json: Json): List<OverviewMetric> =
    json.decodeFromJsonElement<List<WebOverviewItemDto>>(results).map { it.toOverviewMetric() }

internal fun WebOverviewItemDto.toOverviewMetric(): OverviewMetric = OverviewMetric(
    key = when (key) {
        "visitors" -> OverviewMetricKey.Visitors
        "views" -> OverviewMetricKey.Views
        "sessions" -> OverviewMetricKey.Sessions
        "session duration" -> OverviewMetricKey.SessionDuration
        "bounce rate" -> OverviewMetricKey.BounceRate
        else -> OverviewMetricKey.Other
    },
    label = key,
    value = value,
    previous = previous,
    changePercent = changeFromPreviousPct,
    kind = when (kind) {
        "duration_s" -> OverviewMetricKind.DurationSeconds
        "percentage" -> OverviewMetricKind.Percentage
        else -> OverviewMetricKind.Count
    },
    isIncreaseBad = isIncreaseBad ?: false,
)

internal fun QueryResponseDto.toTimeSeries(json: Json): TimeSeries? {
    val first = json.decodeFromJsonElement<List<TrendResultDto>>(results).firstOrNull() ?: return null
    val values = first.data.orEmpty()
    val labels = first.days ?: first.labels ?: emptyList()
    return TimeSeries(values.mapIndexed { index, value -> TimeSeriesPoint(labels.getOrNull(index).orEmpty(), value) })
}

internal fun QueryResponseDto.toBreakdownTable(breakdown: WebBreakdown): BreakdownTable {
    val columns = columns.orEmpty()
    val labelIndex = columns.indexOf(BreakdownColumn)
    val visitorsIndex = columns.indexOf(VisitorsColumn)
    val viewsIndex = columns.indexOf(ViewsColumn)
    val rows = (results as? JsonArray).orEmpty().mapNotNull { row ->
        val cells = row as? JsonArray ?: return@mapNotNull null
        BreakdownRow(
            label = cells.getOrNull(labelIndex).asLabel(),
            visitors = cells.getOrNull(visitorsIndex).firstNumber() ?: 0.0,
            views = cells.getOrNull(viewsIndex).firstNumber(),
        )
    }
    return BreakdownTable(breakdown, rows)
}

/** Cells arrive either as a number or as `[current, previous]`. */
private fun JsonElement?.firstNumber(): Double? = when (this) {
    null, JsonNull -> null
    is JsonPrimitive -> doubleOrNull
    is JsonArray -> firstOrNull()?.let { (it as? JsonPrimitive)?.doubleOrNull }
    else -> null
}

private fun JsonElement?.asLabel(): String = when (this) {
    null, JsonNull -> "(none)"
    is JsonPrimitive -> contentOrNull ?: "(none)"
    is JsonArray -> jsonArray.joinToString(" ") { (it as? JsonPrimitive)?.contentOrNull.orEmpty() }
    else -> toString()
}
