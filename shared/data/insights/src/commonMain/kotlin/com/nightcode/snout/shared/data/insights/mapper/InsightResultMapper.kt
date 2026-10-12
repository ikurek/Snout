package com.nightcode.snout.shared.data.insights.mapper

import com.nightcode.snout.shared.data.common.number
import com.nightcode.snout.shared.data.common.string
import com.nightcode.snout.shared.data.common.stringValue
import com.nightcode.snout.shared.domain.model.insights.InsightDisplay
import com.nightcode.snout.shared.domain.model.insights.InsightKind
import com.nightcode.snout.shared.domain.model.insights.InsightResult
import com.nightcode.snout.shared.domain.model.insights.RetentionCohort
import com.nightcode.snout.shared.domain.model.insights.TrendSeries
import com.nightcode.snout.shared.domain.model.insights.ValueFormat
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlin.time.Clock
import kotlin.time.Instant

private const val MaxTableRows = 50

private val SummableMath = setOf("total", "dau", "unique_session", "unique_group", "sum")

/** Turns PostHog's kind-specific `result` payload into something the UI can draw; null when not computed yet. */
internal fun JsonElement?.toInsightResult(
    query: JsonElement?,
    kind: InsightKind,
    display: InsightDisplay,
    columns: List<String>?,
    now: Instant = Clock.System.now(),
): InsightResult? {
    if (this == null || this is JsonNull) return null
    return when {
        display == InsightDisplay.Unsupported -> InsightResult.Unsupported

        kind == InsightKind.Funnels -> toFunnel()

        kind == InsightKind.Retention -> toRetention(query.retentionPeriod(), now)

        kind == InsightKind.Sql -> toSqlResult(query, display, columns)

        kind == InsightKind.Trends || kind == InsightKind.Stickiness || kind == InsightKind.Lifecycle ->
            toSeries(query.valueFormat())

        else -> InsightResult.Unsupported
    }
}

private fun JsonElement.toSeries(format: ValueFormat): InsightResult {
    val entries = (this as? JsonArray)?.mapNotNull { it as? JsonObject } ?: return InsightResult.Unsupported
    val (previous, current) = entries.partition { it.stringValue("compare_label") == "previous" }
    val previousByLabel = previous.groupBy { it.seriesLabel() }
    val series = current.map { entry ->
        entry.toTrendSeries(previous = previousByLabel[entry.seriesLabel()]?.firstOrNull()?.toTrendSeries())
    }
    return InsightResult.Series(series, format)
}

private fun JsonObject.toTrendSeries(previous: TrendSeries? = null): TrendSeries {
    val values = get("data").numbers()
    // Counts add up across periods; averages, percentiles and active-user windows don't, so they get a mean.
    val math = (get("action") as? JsonObject)?.stringValue("math")
    val summable = math == null || math in SummableMath
    return TrendSeries(
        label = seriesLabel(),
        values = values,
        periods = periods(),
        aggregatedValue = get("aggregated_value").number()
            ?: countWithoutData()
            ?: values.takeUnless { summable }?.ifEmpty { null }?.average(),
        previous = previous,
    )
}

/** Trends send ISO dates in `days`; stickiness sends day counts there and the readable text in `labels`. */
private fun JsonObject.periods(): List<String> {
    val days = get("days") as? JsonArray
    val allStrings = days != null && days.all { (it as? JsonPrimitive)?.isString == true }
    return if (allStrings) days.strings().ifEmpty { get("labels").strings() } else get("labels").strings()
}

private fun JsonObject.countWithoutData(): Double? = get("count").number().takeIf { get("data") == null }

private fun JsonObject.seriesLabel(): String {
    val breakdown = get("breakdown_value").breakdownText()
    val label = stringValue("label")
        ?: (get("action") as? JsonObject)?.let { it.stringValue("custom_name") ?: it.stringValue("name") }
        ?: "Series"
    return if (breakdown != null && breakdown !in label) "$label · $breakdown" else label
}

private fun JsonElement.toRetention(period: DateTimeUnit, now: Instant): InsightResult {
    val entries = (this as? JsonArray)?.mapNotNull { it as? JsonObject } ?: return InsightResult.Unsupported
    val values = entries.map { entry -> (entry["values"] as? JsonArray)?.mapNotNull { it as? JsonObject }.orEmpty() }
    val longest = values.maxByOrNull { it.size }.orEmpty()
    val intervals = longest.mapIndexed { index, value -> value.stringValue("label") ?: index.toString() }
    // Breakdowns repeat each cohort date once per breakdown value; their counts add up to the whole cohort.
    val cohorts = entries.indices
        .groupBy { entries[it].stringValue("date") ?: entries[it].stringValue("label").orEmpty() }
        .map { (date, indices) ->
            val rows = indices.map { index -> values[index].map { it["count"].number() ?: 0.0 } }
            val size = reachedIntervals(date, rows.maxOf { it.size }, period, now)
            RetentionCohort(
                date = date,
                counts = List(size) { interval -> rows.sumOf { it.getOrNull(interval) ?: 0.0 } },
            )
        }
    return InsightResult.Retention(intervals = intervals, cohorts = cohorts)
}

/**
 * PostHog pads every cohort to the full interval count with zeros, even for intervals that haven't started yet.
 * Those aren't drop-offs, so only intervals starting by [now] are kept.
 */
private fun reachedIntervals(date: String, size: Int, period: DateTimeUnit, now: Instant): Int {
    val start = runCatching { Instant.parse(date) }.getOrNull() ?: return size
    return (0 until size).count { start.plus(it, period, TimeZone.UTC) <= now }
}

private fun JsonElement.toSqlResult(
    query: JsonElement?,
    display: InsightDisplay,
    columns: List<String>?,
): InsightResult {
    val rows = this as? JsonArray ?: return InsightResult.Unsupported
    val header = columns ?: (rows.firstOrNull() as? JsonObject)?.keys?.toList().orEmpty()
    val cells = rows.map { row ->
        when (row) {
            is JsonArray -> row.map { it.cellText() }
            is JsonObject -> header.map { row[it].cellText() }
            else -> listOf(row.cellText())
        }
    }
    val visualization = when (display) {
        InsightDisplay.Number -> boldNumber(header, cells, query.valueFormat())
        InsightDisplay.Line, InsightDisplay.Bar -> chartSeries(header, cells, query)
        else -> null
    }
    return visualization ?: InsightResult.Table(
        columns = header,
        rows = cells.take(MaxTableRows),
        totalRows = cells.size,
    )
}

/** A single numeric cell, as SQL "BoldNumber" visualizations expect. */
private fun boldNumber(header: List<String>, cells: List<List<String>>, format: ValueFormat): InsightResult? {
    val value = cells.singleOrNull()?.singleOrNull()?.toDoubleOrNull() ?: return null
    val label = header.singleOrNull().orEmpty()
    return InsightResult.Series(listOf(TrendSeries(label, emptyList(), emptyList(), value)), format)
}

/** One series per chart y column against the x column; null when the chart settings don't match the columns. */
private fun chartSeries(header: List<String>, cells: List<List<String>>, query: JsonElement?): InsightResult? {
    val x = header.indexOf(query.chartXColumn())
    val ys = query.chartYColumns().mapNotNull { axis -> axis.stringValue("column")?.let(header::indexOf) }
        .filter { it >= 0 }
    if (x < 0 || ys.isEmpty()) return null
    val periods = cells.map { it.getOrNull(x).orEmpty() }
    val series = ys.map { y ->
        TrendSeries(
            label = header[y],
            values = cells.map { it.getOrNull(y)?.toDoubleOrNull() ?: 0.0 },
            periods = periods,
            aggregatedValue = null,
        )
    }
    return InsightResult.Series(series, query.valueFormat())
}

private fun JsonElement?.cellText(): String = when (this) {
    null, JsonNull -> ""
    is JsonPrimitive -> contentOrNull ?: toString()
    else -> toString()
}

/** A breakdown value as text; multi-property breakdowns arrive as arrays and read "Chrome / Mac OS X". */
internal fun JsonElement?.breakdownText(): String? = when (this) {
    null, JsonNull -> null
    is JsonPrimitive -> contentOrNull
    is JsonArray -> joinToString(" / ") { it.string().orEmpty() }
    else -> null
}

private fun JsonElement?.numbers(): List<Double> = (this as? JsonArray)?.mapNotNull { it.number() }.orEmpty()

private fun JsonElement?.strings(): List<String> = (this as? JsonArray)?.mapNotNull { it.string() }.orEmpty()
