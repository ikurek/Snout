package com.nightcode.snout.shared.data.insights.mapper

import com.nightcode.snout.shared.domain.model.insights.FunnelStep
import com.nightcode.snout.shared.domain.model.insights.InsightDisplay
import com.nightcode.snout.shared.domain.model.insights.InsightKind
import com.nightcode.snout.shared.domain.model.insights.InsightResult
import com.nightcode.snout.shared.domain.model.insights.TrendSeries
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
    kind: InsightKind,
    display: InsightDisplay,
    columns: List<String>?,
): InsightResult? {
    if (this == null || this is JsonNull) return null
    return when {
        display == InsightDisplay.Unsupported -> InsightResult.Unsupported
        kind == InsightKind.Funnels -> toFunnel()
        kind == InsightKind.Sql -> toTable(columns)
        kind == InsightKind.Trends || kind == InsightKind.Stickiness || kind == InsightKind.Lifecycle -> toSeries()
        else -> InsightResult.Unsupported
    }
}

private fun JsonElement.toSeries(): InsightResult {
    val entries = (this as? JsonArray)?.mapNotNull { it as? JsonObject } ?: return InsightResult.Unsupported
    val current = entries.filter { it.stringValue("compare_label") != "previous" }
    val series = current.map { entry ->
        TrendSeries(
            label = entry.seriesLabel(),
            values = entry["data"].numbers(),
            periods = entry["days"].strings().ifEmpty { entry["labels"].strings() },
            aggregatedValue = entry["aggregated_value"].number() ?: entry.countWithoutData(),
        )
    }
    return InsightResult.Series(series)
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
    // Breakdown funnels nest one step list per breakdown value; the first one is enough to read the shape.
    val steps = (array.firstOrNull() as? JsonArray ?: array).mapNotNull { it as? JsonObject }
    val first = steps.firstOrNull()?.get("count").number() ?: 0.0
    return InsightResult.Funnel(
        steps.map { step ->
            val count = step["count"].number() ?: 0.0
            FunnelStep(
                name = step.stringValue("custom_name") ?: step.stringValue("name") ?: "Step",
                count = count,
                conversionRate = if (first > 0) count / first else 0.0,
                medianConversionSeconds = step["median_conversion_time"].number(),
            )
        },
    )
}

private fun JsonElement.toTable(columns: List<String>?): InsightResult {
    val rows = (this as? JsonArray)?.take(MaxTableRows) ?: return InsightResult.Unsupported
    val header = columns ?: (rows.firstOrNull() as? JsonObject)?.keys?.toList().orEmpty()
    val cells = rows.map { row ->
        when (row) {
            is JsonArray -> row.map { it.cellText() }
            is JsonObject -> header.map { row[it].cellText() }
            else -> listOf(row.cellText())
        }
    }
    return InsightResult.Table(columns = header, rows = cells)
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
