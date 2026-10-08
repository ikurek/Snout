package com.nightcode.snout.shared.data.insights.mapper

import com.nightcode.snout.shared.domain.model.insights.FunnelBreakdown
import com.nightcode.snout.shared.domain.model.insights.FunnelStep
import com.nightcode.snout.shared.domain.model.insights.InsightResult
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

internal fun JsonElement.toFunnel(): InsightResult {
    val array = this as? JsonArray ?: return InsightResult.Unsupported
    // Breakdown funnels nest one step list per breakdown value; their counts add up to the whole funnel.
    val breakdowns = if (array.firstOrNull() is JsonArray) {
        array.map { breakdown -> (breakdown as? JsonArray)?.mapNotNull { it as? JsonObject }.orEmpty() }
    } else {
        listOf(array.mapNotNull { it as? JsonObject })
    }
    val steps = breakdowns.firstOrNull().orEmpty()
    val counts = steps.indices.map { index -> breakdowns.sumOf { it.getOrNull(index)?.get("count").number() ?: 0.0 } }
    return InsightResult.Funnel(
        // Medians of separate breakdowns don't combine into the median of the whole funnel.
        steps = steps.toFunnelSteps(counts, withMedians = breakdowns.size == 1),
        breakdowns = if (breakdowns.size > 1) {
            breakdowns.map { breakdown ->
                FunnelBreakdown(
                    label = breakdown.firstOrNull()?.get("breakdown_value").breakdownText().orEmpty(),
                    steps = breakdown.toFunnelSteps(),
                )
            }
        } else {
            emptyList()
        },
    )
}

/** Steps named after the first breakdown's, with conversion measured against the first of [counts]. */
private fun List<JsonObject>.toFunnelSteps(
    counts: List<Double> = map { it["count"].number() ?: 0.0 },
    withMedians: Boolean = true,
): List<FunnelStep> {
    val first = counts.firstOrNull() ?: 0.0
    return mapIndexed { index, step ->
        FunnelStep(
            name = step.stringValue("custom_name") ?: step.stringValue("name") ?: "Step",
            count = counts[index],
            conversionRate = if (first > 0) counts[index] / first else 0.0,
            medianConversionSeconds = if (withMedians) step["median_conversion_time"].number() else null,
        )
    }
}
