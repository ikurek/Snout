package com.nightcode.snout.features.insights.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.nightcode.snout.common.format.formatCompact
import com.nightcode.snout.common.format.formatDurationSeconds
import com.nightcode.snout.common.format.formatPercent
import com.nightcode.snout.designsystem.charts.FunnelBarStep
import com.nightcode.snout.designsystem.charts.FunnelBars
import com.nightcode.snout.designsystem.charts.RankedBarList
import com.nightcode.snout.designsystem.charts.RankedBarRow
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.shared.domain.model.insights.FunnelBreakdown
import com.nightcode.snout.shared.domain.model.insights.InsightResult
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.insight_result_breakdown_none
import snout.composeapp.generated.resources.insight_result_empty
import snout.composeapp.generated.resources.insight_result_funnel_breakdowns
import snout.composeapp.generated.resources.insight_result_median

@Composable
internal fun FunnelResultView(result: InsightResult.Funnel, detailed: Boolean) {
    if (result.steps.isEmpty()) {
        ResultNote(stringResource(Res.string.insight_result_empty))
        return
    }
    val medianLabel = stringResource(Res.string.insight_result_median)
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.large)) {
        FunnelBars(
            steps = result.steps.map { step ->
                FunnelBarStep(
                    label = step.name,
                    value = step.count,
                    fraction = step.conversionRate,
                    caption = step.medianConversionSeconds?.let { "$medianLabel ${it.formatDurationSeconds()}" },
                )
            },
        )
        if (detailed && result.breakdowns.isNotEmpty()) FunnelBreakdowns(result.breakdowns)
    }
}

/** Whole-funnel conversion per breakdown value, largest groups first so tiny ones don't top the list. */
@Composable
private fun FunnelBreakdowns(breakdowns: List<FunnelBreakdown>) {
    val none = stringResource(Res.string.insight_result_breakdown_none)
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        Text(
            text = stringResource(Res.string.insight_result_funnel_breakdowns),
            style = SnoutTheme.typography.titleSmall,
        )
        ExpandableRows(items = breakdowns.sortedByDescending { it.entered }, expandable = true) { rows ->
            RankedBarList(
                rows = rows.map {
                    RankedBarRow(
                        label = it.label.ifEmpty { none },
                        value = it.conversionRate,
                        secondary = "${it.entered.formatCompact()} → ${it.converted.formatCompact()}",
                    )
                },
                labelIsCode = false,
                valueFormatter = { it.formatPercent(isFraction = true) },
            )
        }
    }
}
