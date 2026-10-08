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
import com.nightcode.snout.common.format.formatDurationSeconds
import com.nightcode.snout.common.format.formatPercent
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
import com.nightcode.snout.designsystem.charts.RetentionTable
import com.nightcode.snout.designsystem.charts.RetentionTableRow
import com.nightcode.snout.designsystem.charts.chartColor
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.shared.domain.model.insights.InsightDisplay
import com.nightcode.snout.shared.domain.model.insights.InsightResult
import com.nightcode.snout.shared.domain.model.insights.TrendSeries
import com.nightcode.snout.shared.domain.model.insights.ValueFormat
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.insight_result_empty
import snout.composeapp.generated.resources.insight_result_median
import snout.composeapp.generated.resources.insight_result_more_rows
import snout.composeapp.generated.resources.insight_result_more_series
import snout.composeapp.generated.resources.insight_result_retention_cohort_size
import snout.composeapp.generated.resources.insight_result_retention_curve
import snout.composeapp.generated.resources.insight_result_unsupported

/** Lines, bars and slices stop being readable past a handful of series. */
private const val MaxChartSeries = 5

/** Ranked lists and tables can take more rows before the rest collapses into "and N more". */
private const val MaxListRows = 10

private const val DateLength = 10

/**
 * Draws an insight result with the closest readable chart for its display type.
 * [detailed] adds what only fits on the insight's own screen, such as the retention cohort table.
 */
@Composable
internal fun InsightResultView(
    result: InsightResult,
    display: InsightDisplay,
    chartHeight: Dp = 180.dp,
    detailed: Boolean = false,
) {
    when (result) {
        InsightResult.Unsupported -> Note(stringResource(Res.string.insight_result_unsupported))

        is InsightResult.Series -> SeriesView(result.series, result.format, display, chartHeight)

        is InsightResult.Funnel -> FunnelView(result)

        is InsightResult.Retention -> RetentionView(result, chartHeight, detailed)

        is InsightResult.Table -> if (result.rows.isEmpty()) {
            Note(stringResource(Res.string.insight_result_empty))
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DataTable(columns = result.columns, rows = result.rows.take(MaxListRows))
                MoreNote(Res.string.insight_result_more_rows, result.totalRows - MaxListRows)
            }
        }
    }
}

@Composable
private fun SeriesView(all: List<TrendSeries>, format: ValueFormat, display: InsightDisplay, chartHeight: Dp) {
    if (all.isEmpty() || all.all { it.values.isEmpty() && it.aggregatedValue == null }) {
        Note(stringResource(Res.string.insight_result_empty))
        return
    }
    val formatter: (Double) -> String = { it.formatValue(format) }
    val charted = all.take(MaxChartSeries)
    val chartSeries = charted.mapIndexed { index, s -> ChartSeries(s.label, s.values, chartColor(index)) }
    val xLabels = charted.maxBy { it.periods.size }.periods.map { it.formatChartLabel() }
    val ranked = all.sortedByDescending { it.total }.take(MaxListRows)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when (display) {
            InsightDisplay.Line ->
                LineChart(series = chartSeries, xLabels = xLabels, height = chartHeight, valueFormatter = formatter)

            InsightDisplay.Bar ->
                BarChart(series = chartSeries, xLabels = xLabels, height = chartHeight, valueFormatter = formatter)

            InsightDisplay.Pie -> DonutChart(
                slices = charted.mapIndexed { index, s -> ChartSlice(s.label, s.total, chartColor(index)) },
                valueFormatter = formatter,
            )

            InsightDisplay.Number -> NumberView(ranked, formatter)

            InsightDisplay.BarValue, InsightDisplay.Table, InsightDisplay.Funnel, InsightDisplay.Unsupported ->
                RankedBarList(
                    rows = ranked.map { RankedBarRow(label = it.label, value = it.total) },
                    valueFormatter = formatter,
                )
        }
        when (display) {
            InsightDisplay.Line, InsightDisplay.Bar, InsightDisplay.Pie ->
                MoreNote(Res.string.insight_result_more_series, all.size - charted.size)

            else -> MoreNote(Res.string.insight_result_more_rows, all.size - ranked.size)
        }
    }
}

@Composable
private fun NumberView(series: List<TrendSeries>, formatter: (Double) -> String) {
    if (series.size == 1) {
        BoldNumber(value = formatter(series.single().total), label = series.single().label.ifEmpty { null })
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
                    Text(text = formatter(s.total), style = SnoutTheme.typography.headlineMedium)
                }
            }
        }
    }
}

@Composable
private fun RetentionView(result: InsightResult.Retention, chartHeight: Dp, detailed: Boolean) {
    val curve = result.curve
    if (curve.isEmpty()) {
        Note(stringResource(Res.string.insight_result_empty))
        return
    }
    // Intervals no cohort has reached yet would only add empty columns.
    val intervals = result.intervals.take(curve.size)
    val percent: (Double) -> String = { it.formatPercent(isFraction = true) }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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

@Composable
private fun MoreNote(text: StringResource, count: Int) {
    if (count > 0) Note(stringResource(text, count))
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
