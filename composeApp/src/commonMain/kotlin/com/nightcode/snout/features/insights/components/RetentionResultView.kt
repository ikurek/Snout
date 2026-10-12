package com.nightcode.snout.features.insights.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import com.nightcode.snout.common.format.formatChartLabel
import com.nightcode.snout.common.format.formatPercent
import com.nightcode.snout.designsystem.charts.ChartSeries
import com.nightcode.snout.designsystem.charts.LineChart
import com.nightcode.snout.designsystem.charts.RetentionTable
import com.nightcode.snout.designsystem.charts.RetentionTableRow
import com.nightcode.snout.designsystem.charts.chartColor
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.shared.domain.model.insights.InsightResult
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.insight_result_empty
import snout.composeapp.generated.resources.insight_result_retention_cohort_size
import snout.composeapp.generated.resources.insight_result_retention_curve

private const val DateLength = 10

@Composable
internal fun RetentionResultView(result: InsightResult.Retention, chartHeight: Dp, detailed: Boolean) {
    val curve = result.curve
    if (curve.isEmpty()) {
        ResultNote(stringResource(Res.string.insight_result_empty))
        return
    }
    // Intervals no cohort has reached yet would only add empty columns.
    val intervals = result.intervals.take(curve.size)
    val percent: (Double) -> String = { it.formatPercent(isFraction = true) }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.large)) {
        LineChart(
            series = listOf(
                ChartSeries(stringResource(Res.string.insight_result_retention_curve), curve, chartColor(0)),
            ),
            xLabels = intervals,
            height = chartHeight,
            valueFormatter = percent,
        )
        if (detailed) {
            RetentionTable(
                sizeLabel = stringResource(Res.string.insight_result_retention_cohort_size),
                intervals = intervals,
                rows = result.cohorts.map { cohort ->
                    RetentionTableRow(
                        label = cohort.date.cohortLabel(),
                        size = cohort.size,
                        rates = intervals.indices.map { cohort.rate(it) },
                    )
                },
            )
        }
    }
}

/** Cohorts start at midnight for daily and longer periods; only hourly cohorts need their time. */
private fun String.cohortLabel(): String {
    val midnight = length <= DateLength || drop(DateLength + 1).startsWith("00:00:00")
    return if (midnight) take(DateLength).formatChartLabel() else formatChartLabel()
}
