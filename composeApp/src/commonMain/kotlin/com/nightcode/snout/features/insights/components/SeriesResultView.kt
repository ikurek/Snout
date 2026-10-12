package com.nightcode.snout.features.insights.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import com.nightcode.snout.common.format.formatChartLabel
import com.nightcode.snout.common.format.formatSigned
import com.nightcode.snout.designsystem.atoms.TrendChip
import com.nightcode.snout.designsystem.charts.BarChart
import com.nightcode.snout.designsystem.charts.ChartSeries
import com.nightcode.snout.designsystem.charts.ChartSlice
import com.nightcode.snout.designsystem.charts.DonutChart
import com.nightcode.snout.designsystem.charts.LineChart
import com.nightcode.snout.designsystem.charts.RankedBarList
import com.nightcode.snout.designsystem.charts.RankedBarRow
import com.nightcode.snout.designsystem.charts.chartColor
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.shared.domain.model.insights.InsightDisplay
import com.nightcode.snout.shared.domain.model.insights.TrendSeries
import com.nightcode.snout.shared.domain.model.insights.ValueFormat
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.insight_result_empty
import snout.composeapp.generated.resources.insight_result_more_series
import snout.composeapp.generated.resources.insight_result_previous_lines
import snout.composeapp.generated.resources.insight_result_previous_of
import snout.composeapp.generated.resources.insight_result_previous_period
import snout.composeapp.generated.resources.insight_result_this_period

/** Lines, bars and slices stop being readable past a handful of series. */
private const val MaxChartSeries = 5

@Composable
internal fun SeriesResultView(
    all: List<TrendSeries>,
    format: ValueFormat,
    display: InsightDisplay,
    chartHeight: Dp,
    detailed: Boolean,
) {
    if (all.isEmpty() || all.all { it.values.isEmpty() && it.aggregatedValue == null }) {
        ResultNote(stringResource(Res.string.insight_result_empty))
        return
    }
    val formatter: (Double) -> String = { it.formatValue(format) }
    val charted = all.take(MaxChartSeries)
    val ranked = all.sortedByDescending { it.total }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
        SeriesChart(charted, ranked, formatter, display, chartHeight, detailed)
        val chart = display == InsightDisplay.Line || display == InsightDisplay.Bar || display == InsightDisplay.Pie
        // The legend names each line but not its total; the detail screen lists every series with it.
        val listAll = detailed && if (display == InsightDisplay.Pie) all.size > charted.size else all.size > 1
        when {
            !chart -> Unit
            listAll -> SeriesList(ranked, formatter, expandable = true)
            else -> MoreNote(Res.string.insight_result_more_series, all.size - charted.size)
        }
    }
}

/** The display's own chart; number and list displays take every series in [ranked] order. */
@Composable
private fun SeriesChart(
    charted: List<TrendSeries>,
    ranked: List<TrendSeries>,
    formatter: (Double) -> String,
    display: InsightDisplay,
    chartHeight: Dp,
    detailed: Boolean,
) {
    val xLabels = charted.maxBy { it.periods.size }.periods.map { it.formatChartLabel() }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
        when (display) {
            InsightDisplay.Line -> {
                LineChart(
                    series = lineSeries(charted),
                    xLabels = xLabels,
                    height = chartHeight,
                    valueFormatter = formatter,
                )
                if (charted.size > 1 && charted.any { it.previous != null }) {
                    ResultNote(stringResource(Res.string.insight_result_previous_lines))
                }
            }

            InsightDisplay.Bar -> BarChart(
                series = charted.mapIndexed { index, s -> ChartSeries(s.label, s.values, chartColor(index)) },
                xLabels = xLabels,
                height = chartHeight,
                valueFormatter = formatter,
            )

            InsightDisplay.Pie -> DonutChart(
                slices = charted.mapIndexed { index, s -> ChartSlice(s.label, s.total, chartColor(index)) },
                valueFormatter = formatter,
            )

            InsightDisplay.Number -> NumberResultView(ranked, formatter, detailed)

            InsightDisplay.BarValue, InsightDisplay.Table, InsightDisplay.Funnel, InsightDisplay.Unsupported ->
                SeriesList(ranked, formatter, detailed)
        }
    }
}

/** Charted series, each followed by its previous period as a dashed line in the same colour. */
@Composable
private fun lineSeries(charted: List<TrendSeries>): List<ChartSeries> {
    val single = charted.singleOrNull()
    val previous = single?.previous
    if (single != null && previous != null) {
        return listOf(
            ChartSeries(stringResource(Res.string.insight_result_this_period), single.values, chartColor(0)),
            ChartSeries(
                name = stringResource(Res.string.insight_result_previous_period),
                values = previous.values,
                color = SnoutTheme.colors.onSurfaceVariant,
                dashed = true,
                filled = false,
            ),
        )
    }
    return charted.flatMapIndexed { index, s ->
        val color = chartColor(index)
        listOfNotNull(
            ChartSeries(s.label, s.values, color),
            s.previous?.let {
                ChartSeries(
                    name = stringResource(Res.string.insight_result_previous_of, s.label),
                    values = it.values,
                    color = color,
                    dashed = true,
                    filled = false,
                )
            },
        )
    }
}

@Composable
private fun SeriesList(ranked: List<TrendSeries>, formatter: (Double) -> String, expandable: Boolean) {
    ExpandableRows(items = ranked, expandable = expandable) { rows ->
        RankedBarList(
            rows = rows.map { RankedBarRow(label = it.label, value = it.total, secondary = it.changeText()) },
            valueFormatter = formatter,
        )
    }
}

/** "+12.5%" against the previous period, for rows too tight for a [TrendChip]. */
private fun TrendSeries.changeText(): String? = changePercent?.let { "${it.formatSigned()}%" }
