package com.nightcode.snout.shared.data.insights.mapper

import com.nightcode.snout.shared.domain.model.insights.InsightDisplay
import com.nightcode.snout.shared.domain.model.insights.InsightKind
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

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

        InsightKind.Sql -> InsightDisplay.Table

        InsightKind.Retention, InsightKind.Paths, InsightKind.Unknown -> InsightDisplay.Unsupported
    }
}

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

internal fun JsonObject.stringValue(key: String): String? = (get(key) as? JsonPrimitive)?.contentOrNull
