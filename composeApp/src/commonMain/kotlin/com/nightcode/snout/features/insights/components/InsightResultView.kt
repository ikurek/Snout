package com.nightcode.snout.features.insights.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.format.formatChartLabel
import com.nightcode.snout.common.format.formatCompact
import com.nightcode.snout.common.format.formatDurationSeconds
import com.nightcode.snout.designsystem.charts.BarChart
import com.nightcode.snout.designsystem.charts.BoldNumber
import com.nightcode.snout.designsystem.charts.ChartSeries
import com.nightcode.snout.designsystem.charts.ChartSlice
import com.nightcode.snout.designsystem.charts.DataTable
import com.nightcode.snout.designsystem.charts.DonutChart
import com.nightcode.snout.designsystem.charts.FunnelBarStep
import com.nightcode.snout.designsystem.charts.FunnelBars
import com.nightcode.snout.designsystem.charts.LineChart
import com.nightcode.snout.designsystem.charts.RankedBarList
import com.nightcode.snout.designsystem.charts.RankedBarRow
import com.nightcode.snout.designsystem.charts.chartColor
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.shared.domain.model.insights.InsightDisplay
import com.nightcode.snout.shared.domain.model.insights.InsightResult
import com.nightcode.snout.shared.domain.model.insights.TrendSeries
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.insight_result_empty
import snout.composeapp.generated.resources.insight_result_median
import snout.composeapp.generated.resources.insight_result_unsupported

private const val MaxSeries = 5

/** Draws an insight result with the closest readable chart for its display type. */
@Composable
internal fun InsightResultView(result: InsightResult, display: InsightDisplay, chartHeight: Dp = 180.dp) {
    when (result) {
        InsightResult.Unsupported -> Note(stringResource(Res.string.insight_result_unsupported))

        is InsightResult.Series -> SeriesView(result.series, display, chartHeight)

        is InsightResult.Funnel -> FunnelView(result)

        is InsightResult.Table -> if (result.rows.isEmpty()) {
            Note(stringResource(Res.string.insight_result_empty))
        } else {
            DataTable(columns = result.columns, rows = result.rows)
        }
    }
}

@Composable
private fun SeriesView(all: List<TrendSeries>, display: InsightDisplay, chartHeight: Dp) {
    val series = all.take(MaxSeries)
    if (series.isEmpty() || series.all { it.values.isEmpty() && it.aggregatedValue == null }) {
        Note(stringResource(Res.string.insight_result_empty))
        return
    }
    val chartSeries = series.mapIndexed { index, s -> ChartSeries(s.label, s.values, chartColor(index)) }
    val xLabels = series.maxBy { it.periods.size }.periods.map { it.formatChartLabel() }
    when (display) {
        InsightDisplay.Line -> LineChart(series = chartSeries, xLabels = xLabels, height = chartHeight)

        InsightDisplay.Bar -> BarChart(series = chartSeries, xLabels = xLabels, height = chartHeight)

        InsightDisplay.Number -> NumberView(series)

        InsightDisplay.Pie -> DonutChart(
            slices = series.mapIndexed { index, s -> ChartSlice(s.label, s.total, chartColor(index)) },
        )

        InsightDisplay.BarValue, InsightDisplay.Table, InsightDisplay.Funnel, InsightDisplay.Unsupported ->
            RankedBarList(rows = series.map { RankedBarRow(label = it.label, value = it.total) })
    }
}

@Composable
private fun NumberView(series: List<TrendSeries>) {
    if (series.size == 1) {
        BoldNumber(value = series.single().total.formatCompact(), label = series.single().label)
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            series.forEach { s ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = s.label,
                        style = SnoutTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(text = s.total.formatCompact(), style = SnoutTheme.typography.headlineMedium)
                }
            }
        }
    }
}

@Composable
private fun FunnelView(result: InsightResult.Funnel) {
    if (result.steps.isEmpty()) {
        Note(stringResource(Res.string.insight_result_empty))
        return
    }
    val medianLabel = stringResource(Res.string.insight_result_median)
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
}

@Composable
private fun Note(text: String) {
    Text(text = text, style = SnoutTheme.typography.bodyMedium, color = SnoutTheme.colors.onSurfaceVariant)
}
