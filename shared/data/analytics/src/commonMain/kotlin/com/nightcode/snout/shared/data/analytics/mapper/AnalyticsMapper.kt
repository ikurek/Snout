package com.nightcode.snout.shared.data.analytics.mapper

import com.nightcode.snout.shared.api.analytics.TrendSeriesNames
import com.nightcode.snout.shared.api.analytics.model.QueryResponseDto
import com.nightcode.snout.shared.api.analytics.model.TrendResultDto
import com.nightcode.snout.shared.api.analytics.model.WebOverviewItemDto
import com.nightcode.snout.shared.data.common.number
import com.nightcode.snout.shared.data.common.string
import com.nightcode.snout.shared.domain.model.analytics.BreakdownRow
import com.nightcode.snout.shared.domain.model.analytics.BreakdownTable
import com.nightcode.snout.shared.domain.model.analytics.MetricTrend
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetric
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKey
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKind
import com.nightcode.snout.shared.domain.model.analytics.TimeSeries
import com.nightcode.snout.shared.domain.model.analytics.TimeSeriesPoint
import com.nightcode.snout.shared.domain.model.analytics.TrendMetric
import com.nightcode.snout.shared.domain.model.analytics.WebBreakdown
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.doubleOrNull

private const val BreakdownColumn = "context.columns.breakdown_value"
private const val VisitorsColumn = "context.columns.visitors"
private const val ViewsColumn = "context.columns.views"
private const val PreviousCompareLabel = "previous"
private const val PercentFactor = 100.0

/** Values PostHog uses for "no value" and "everything else" buckets in breakdowns; neither belongs in a top list. */
private val BreakdownSentinels = setOf("\$\$_posthog_breakdown_null_\$\$", "\$\$_posthog_breakdown_other_\$\$")

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

/** Visitors, views and sessions series with their previous-period counterparts, keyed by the requested name. */
internal fun QueryResponseDto.toMetricTrends(json: Json): List<MetricTrend> {
    val items = json.decodeFromJsonElement<List<TrendResultDto>>(results)
    return listOf(
        TrendMetric.Visitors to TrendSeriesNames.Visitors,
        TrendMetric.Views to TrendSeriesNames.Views,
        TrendMetric.Sessions to TrendSeriesNames.Sessions,
    ).mapNotNull { (metric, name) ->
        val current = items.named(name).current().sumSeries() ?: return@mapNotNull null
        MetricTrend(metric = metric, current = current, previous = items.named(name).previous().sumSeries())
    }
}

/** Rolling active users: the last point of the range is the value "as of" its end, compared to the period before. */
internal fun QueryResponseDto.toActiveUserMetrics(json: Json): List<OverviewMetric> {
    val items = json.decodeFromJsonElement<List<TrendResultDto>>(results)
    return listOf(
        Triple(OverviewMetricKey.WeeklyActiveUsers, TrendSeriesNames.WeeklyActiveUsers, "weekly active users"),
        Triple(OverviewMetricKey.MonthlyActiveUsers, TrendSeriesNames.MonthlyActiveUsers, "monthly active users"),
    ).mapNotNull { (key, name, label) ->
        val value = items.named(name).current().sumSeries()?.points?.lastOrNull()?.value ?: return@mapNotNull null
        val previous = items.named(name).previous().sumSeries()?.points?.lastOrNull()?.value
        OverviewMetric(
            key = key,
            label = label,
            value = value,
            previous = previous,
            changePercent = previous?.takeIf { it > 0 }?.let { (value - it) / it * PercentFactor },
            kind = OverviewMetricKind.Count,
            isIncreaseBad = false,
        )
    }
}

/** Sums the per-event series into one; a project rarely has both page views and screens on the same day. */
internal fun List<TrendResultDto>.sumSeries(): TimeSeries? {
    val series = filter { it.data != null }
    val longest = series.maxByOrNull { it.data.orEmpty().size } ?: return null
    val labels = longest.days ?: longest.labels ?: emptyList()
    val points = longest.data.orEmpty().indices.map { index ->
        TimeSeriesPoint(labels.getOrNull(index).orEmpty(), series.sumOf { it.data?.getOrNull(index) ?: 0.0 })
    }
    return TimeSeries(points)
}

private fun List<TrendResultDto>.named(name: String) = filter { it.action?.customName == name }

private fun List<TrendResultDto>.current() = filterNot { it.compareLabel == PreviousCompareLabel }

private fun List<TrendResultDto>.previous() = filter { it.compareLabel == PreviousCompareLabel }

internal fun QueryResponseDto.toBreakdownTable(breakdown: WebBreakdown): BreakdownTable {
    val columns = columns.orEmpty()
    val labelIndex = columns.indexOf(BreakdownColumn)
    val visitorsIndex = columns.indexOf(VisitorsColumn)
    val viewsIndex = columns.indexOf(ViewsColumn)
    val rows = (results as? JsonArray).orEmpty().mapNotNull { row ->
        val cells = row as? JsonArray ?: return@mapNotNull null
        // A null breakdown is the "everything else" bucket; it says nothing in a top list.
        val label = cells.getOrNull(labelIndex).asLabel() ?: return@mapNotNull null
        BreakdownRow(
            label = label,
            visitors = cells.getOrNull(visitorsIndex).firstNumber() ?: 0.0,
            views = cells.getOrNull(viewsIndex).firstNumber(),
        )
    }
    return BreakdownTable(breakdown, rows)
}

/** Unique visitors per property value from a totals-table trends result, both visit events summed. */
internal fun QueryResponseDto.toPropertyBreakdownTable(json: Json, breakdown: WebBreakdown): BreakdownTable {
    val items = json.decodeFromJsonElement<List<TrendResultDto>>(results)
    val rows = items
        .mapNotNull { item ->
            val label = item.breakdownValue.asLabel()?.takeIf { it.isNotBlank() && it !in BreakdownSentinels }
            label?.let { it to (item.aggregatedValue ?: item.count ?: 0.0) }
        }
        .groupBy({ it.first }, { it.second })
        .map { (label, values) -> BreakdownRow(label = label, visitors = values.sum(), views = null) }
        .sortedByDescending { it.visitors }
    return BreakdownTable(breakdown, rows)
}

/** Cells arrive either as a number or as `[current, previous]`. */
private fun JsonElement?.firstNumber(): Double? = when (this) {
    null, JsonNull -> null
    is JsonPrimitive -> doubleOrNull
    is JsonArray -> firstOrNull().number()
    else -> null
}

private fun JsonElement?.asLabel(): String? = when (this) {
    null, JsonNull -> null
    is JsonPrimitive -> contentOrNull
    is JsonArray -> joinToString(" ") { it.string().orEmpty() }
    else -> toString()
}
