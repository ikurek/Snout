package com.nightcode.snout.shared.data.insights.mapper

import com.nightcode.snout.shared.domain.model.insights.InsightDisplay
import com.nightcode.snout.shared.domain.model.insights.InsightKind
import com.nightcode.snout.shared.domain.model.insights.ValueFormat
import com.nightcode.snout.shared.domain.model.insights.ValueUnit
import kotlinx.datetime.DateTimeUnit
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

/** The query node that actually describes the visualization (unwrapping InsightVizNode / DataTableNode). */
internal fun JsonElement?.sourceNode(): JsonObject? {
    val node = this as? JsonObject ?: return null
    val kind = node.stringValue("kind")
    return if (kind == "InsightVizNode" || kind == "DataTableNode" || kind == "DataVisualizationNode") {
        node["source"] as? JsonObject ?: node
    } else {
        node
    }
}

internal fun JsonElement?.insightKind(): InsightKind = when (sourceNode()?.stringValue("kind")) {
    "TrendsQuery" -> InsightKind.Trends
    "FunnelsQuery" -> InsightKind.Funnels
    "RetentionQuery" -> InsightKind.Retention
    "PathsQuery", "PathsV2Query" -> InsightKind.Paths
    "StickinessQuery" -> InsightKind.Stickiness
    "LifecycleQuery" -> InsightKind.Lifecycle
    "HogQLQuery" -> InsightKind.Sql
    else -> InsightKind.Unknown
}

internal fun JsonElement?.insightDisplay(kind: InsightKind): InsightDisplay {
    val source = sourceNode()
    return when (kind) {
        InsightKind.Trends -> (source?.get("trendsFilter") as? JsonObject).displayOrDefault(InsightDisplay.Line)

        InsightKind.Stickiness -> (source?.get("stickinessFilter") as? JsonObject).displayOrDefault(InsightDisplay.Bar)

        InsightKind.Lifecycle -> InsightDisplay.Bar

        InsightKind.Funnels -> {
            val vizType = (source?.get("funnelsFilter") as? JsonObject)?.stringValue("funnelVizType") ?: "steps"
            if (vizType == "steps") InsightDisplay.Funnel else InsightDisplay.Unsupported
        }

        // DataVisualizationNode keeps the display on the wrapper, not on the HogQL source.
        InsightKind.Sql -> when ((this as? JsonObject).displayOrDefault(InsightDisplay.Table)) {
            InsightDisplay.Line -> InsightDisplay.Line
            InsightDisplay.Bar -> InsightDisplay.Bar
            InsightDisplay.Number -> InsightDisplay.Number
            else -> InsightDisplay.Table
        }

        // Retention is always drawn as its curve; the cohort table comes with it on the detail screen.
        InsightKind.Retention -> InsightDisplay.Line

        InsightKind.Paths, InsightKind.Unknown -> InsightDisplay.Unsupported
    }
}

/** Trends' aggregation axis format, or the first y column's formatting of a SQL visualization. */
internal fun JsonElement?.valueFormat(): ValueFormat =
    (sourceNode()?.get("trendsFilter") as? JsonObject)?.trendsValueFormat() ?: chartValueFormat()

private fun JsonObject.trendsValueFormat(): ValueFormat = ValueFormat(
    unit = when (stringValue("aggregationAxisFormat")) {
        "duration" -> ValueUnit.Duration
        "duration_ms" -> ValueUnit.DurationMs
        "percentage" -> ValueUnit.Percentage
        "percentage_scaled" -> ValueUnit.PercentageScaled
        else -> ValueUnit.Numeric
    },
    prefix = stringValue("aggregationAxisPrefix")?.takeIf { it.isNotEmpty() },
    postfix = stringValue("aggregationAxisPostfix")?.takeIf { it.isNotEmpty() },
    decimalPlaces = intValue("decimalPlaces"),
)

private fun JsonElement?.chartValueFormat(): ValueFormat {
    val settings = chartYColumns().firstOrNull()?.get("settings") as? JsonObject
    val formatting = settings?.get("formatting") as? JsonObject ?: return ValueFormat()
    return ValueFormat(
        prefix = formatting.stringValue("prefix")?.takeIf { it.isNotEmpty() },
        postfix = formatting.stringValue("suffix")?.takeIf { it.isNotEmpty() },
        decimalPlaces = formatting.intValue("decimalPlaces"),
    )
}

/** How long one retention interval lasts; PostHog defaults to days. */
internal fun JsonElement?.retentionPeriod(): DateTimeUnit =
    when ((sourceNode()?.get("retentionFilter") as? JsonObject)?.stringValue("period")) {
        "Hour" -> DateTimeUnit.HOUR
        "Week" -> DateTimeUnit.WEEK
        "Month" -> DateTimeUnit.MONTH
        else -> DateTimeUnit.DAY
    }

/** The x column of a SQL visualization's chart settings. */
internal fun JsonElement?.chartXColumn(): String? =
    (chartSettings()?.get("xAxis") as? JsonObject)?.stringValue("column")

/** The y axis entries of a SQL visualization's chart settings, each with a `column`. */
internal fun JsonElement?.chartYColumns(): List<JsonObject> =
    (chartSettings()?.get("yAxis") as? JsonArray)?.mapNotNull { it as? JsonObject }.orEmpty()

private fun JsonElement?.chartSettings(): JsonObject? = (this as? JsonObject)?.get("chartSettings") as? JsonObject

private fun JsonObject?.displayOrDefault(default: InsightDisplay): InsightDisplay =
    when (this?.stringValue("display")) {
        null -> default
        "ActionsLineGraph", "ActionsLineGraphCumulative", "ActionsAreaGraph" -> InsightDisplay.Line
        "ActionsBar", "ActionsStackedBar", "ActionsUnstackedBar" -> InsightDisplay.Bar
        "ActionsBarValue" -> InsightDisplay.BarValue
        "BoldNumber", "Metric" -> InsightDisplay.Number
        "ActionsPie", "ActionsDonut" -> InsightDisplay.Pie
        "ActionsTable", "WorldMap" -> InsightDisplay.Table
        else -> InsightDisplay.Unsupported
    }

/** "custom name · event" of the query series, used when an insight was never named. */
internal fun JsonElement?.seriesTitle(): String? {
    val series = sourceNode()?.get("series") as? JsonArray ?: return null
    return series.mapNotNull { node ->
        val step = node as? JsonObject ?: return@mapNotNull null
        step.stringValue("custom_name") ?: step.stringValue("name") ?: step.stringValue("event")
    }.distinct().joinToString(" · ").ifBlank { null }
}

internal fun JsonObject.stringValue(key: String): String? = (get(key) as? JsonPrimitive)?.contentOrNull

private fun JsonObject.intValue(key: String): Int? = (get(key) as? JsonPrimitive)?.intOrNull
