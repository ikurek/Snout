package com.nightcode.snout.shared.data.insights.mapper

import com.nightcode.snout.shared.domain.model.insights.FunnelStep
import com.nightcode.snout.shared.domain.model.insights.InsightDisplay
import com.nightcode.snout.shared.domain.model.insights.InsightKind
import com.nightcode.snout.shared.domain.model.insights.InsightResult
import com.nightcode.snout.shared.domain.model.insights.RetentionCohort
import com.nightcode.snout.shared.domain.model.insights.TrendSeries
import com.nightcode.snout.shared.domain.model.insights.ValueFormat
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull

private const val MaxTableRows = 50

/** Turns PostHog's kind-specific `result` payload into something the UI can draw; null when not computed yet. */
internal fun JsonElement?.toInsightResult(
    query: JsonElement?,
    kind: InsightKind,
    display: InsightDisplay,
    columns: List<String>?,
): InsightResult? {
    if (this == null || this is JsonNull) return null
    return when {
        display == InsightDisplay.Unsupported -> InsightResult.Unsupported

        kind == InsightKind.Funnels -> toFunnel()

        kind == InsightKind.Retention -> toRetention()

        kind == InsightKind.Sql -> toSqlResult(query, display, columns)

        kind == InsightKind.Trends || kind == InsightKind.Stickiness || kind == InsightKind.Lifecycle ->
            toSeries(query.valueFormat())

        else -> InsightResult.Unsupported
    }
}

private fun JsonElement.toSeries(format: ValueFormat): InsightResult {
    val entries = (this as? JsonArray)?.mapNotNull { it as? JsonObject } ?: return InsightResult.Unsupported
    val current = entries.filter { it.stringValue("compare_label") != "previous" }
    val series = current.map { entry ->
        TrendSeries(
            label = entry.seriesLabel(),
            values = entry["data"].numbers(),
            periods = entry.periods(),
            aggregatedValue = entry["aggregated_value"].number() ?: entry.countWithoutData(),
        )
    }
    return InsightResult.Series(series, format)
}

/** Trends send ISO dates in `days`; stickiness sends day counts there and the readable text in `labels`. */
private fun JsonObject.periods(): List<String> {
    val days = get("days") as? JsonArray
    val allStrings = days != null && days.all { (it as? JsonPrimitive)?.isString == true }
    return if (allStrings) days.strings().ifEmpty { get("labels").strings() } else get("labels").strings()
}

private fun JsonObject.countWithoutData(): Double? = get("count").number().takeIf { get("data") == null }

private fun JsonObject.seriesLabel(): String {
    val breakdown = get("breakdown_value").let { value ->
        when (value) {
            null, JsonNull -> null
            is JsonPrimitive -> value.contentOrNull
            is JsonArray -> value.joinToString(" / ") { (it as? JsonPrimitive)?.contentOrNull.orEmpty() }
            else -> null
        }
    }
    val label = stringValue("label")
        ?: (get("action") as? JsonObject)?.let { it.stringValue("custom_name") ?: it.stringValue("name") }
        ?: "Series"
    return if (breakdown != null && breakdown !in label) "$label · $breakdown" else label
}

private fun JsonElement.toFunnel(): InsightResult {
    val array = this as? JsonArray ?: return InsightResult.Unsupported
    // Breakdown funnels nest one step list per breakdown value; their counts add up to the whole funnel.
    val breakdowns = if (array.firstOrNull() is JsonArray) {
        array.map { breakdown -> (breakdown as? JsonArray)?.mapNotNull { it as? JsonObject }.orEmpty() }
    } else {
        listOf(array.mapNotNull { it as? JsonObject })
    }
    val steps = breakdowns.firstOrNull().orEmpty()
    val counts = steps.indices.map { index -> breakdowns.sumOf { it.getOrNull(index)?.get("count").number() ?: 0.0 } }
    val first = counts.firstOrNull() ?: 0.0
    return InsightResult.Funnel(
        steps.mapIndexed { index, step ->
            FunnelStep(
                name = step.stringValue("custom_name") ?: step.stringValue("name") ?: "Step",
                count = counts[index],
                conversionRate = if (first > 0) counts[index] / first else 0.0,
                // Medians of separate breakdowns don't combine into the median of the whole funnel.
                medianConversionSeconds = if (breakdowns.size == 1) step["median_conversion_time"].number() else null,
            )
        },
    )
}

private fun JsonElement.toRetention(): InsightResult {
    val entries = (this as? JsonArray)?.mapNotNull { it as? JsonObject } ?: return InsightResult.Unsupported
    val values = entries.map { entry -> (entry["values"] as? JsonArray)?.mapNotNull { it as? JsonObject }.orEmpty() }
    val longest = values.maxByOrNull { it.size }.orEmpty()
    val intervals = longest.mapIndexed { index, value -> value.stringValue("label") ?: index.toString() }
    // Breakdowns repeat each cohort date once per breakdown value; their counts add up to the whole cohort.
    val cohorts = entries.indices
        .groupBy { entries[it].stringValue("date") ?: entries[it].stringValue("label").orEmpty() }
        .map { (date, indices) ->
            val rows = indices.map { index -> values[index].map { it["count"].number() ?: 0.0 } }
            val size = rows.maxOf { it.size }
            RetentionCohort(
                date = date,
                counts = List(size) { interval -> rows.sumOf { it.getOrNull(interval) ?: 0.0 } },
            )
        }
    return InsightResult.Retention(intervals = intervals, cohorts = cohorts)
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

private fun JsonElement?.number(): Double? = (this as? JsonPrimitive)?.doubleOrNull

private fun JsonElement?.numbers(): List<Double> = (this as? JsonArray)?.mapNotNull { it.number() }.orEmpty()

private fun JsonElement?.strings(): List<String> =
    (this as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }.orEmpty()
