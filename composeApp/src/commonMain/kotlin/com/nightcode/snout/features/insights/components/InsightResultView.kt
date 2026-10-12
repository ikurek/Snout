package com.nightcode.snout.features.insights.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.format.formatChartLabel
import com.nightcode.snout.common.format.formatCompact
import com.nightcode.snout.common.format.formatDurationSeconds
import com.nightcode.snout.common.format.formatPercent
import com.nightcode.snout.common.format.formatSigned
import com.nightcode.snout.designsystem.atoms.TrendChip
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
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.shared.domain.model.insights.FunnelBreakdown
import com.nightcode.snout.shared.domain.model.insights.InsightDisplay
import com.nightcode.snout.shared.domain.model.insights.InsightResult
import com.nightcode.snout.shared.domain.model.insights.TrendSeries
import com.nightcode.snout.shared.domain.model.insights.ValueFormat
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.insight_result_breakdown_none
import snout.composeapp.generated.resources.insight_result_empty
import snout.composeapp.generated.resources.insight_result_funnel_breakdowns
import snout.composeapp.generated.resources.insight_result_median
import snout.composeapp.generated.resources.insight_result_more_rows
import snout.composeapp.generated.resources.insight_result_more_series
import snout.composeapp.generated.resources.insight_result_previous_lines
import snout.composeapp.generated.resources.insight_result_previous_of
import snout.composeapp.generated.resources.insight_result_previous_period
import snout.composeapp.generated.resources.insight_result_retention_cohort_size
import snout.composeapp.generated.resources.insight_result_retention_curve
import snout.composeapp.generated.resources.insight_result_show_all
import snout.composeapp.generated.resources.insight_result_show_fewer
import snout.composeapp.generated.resources.insight_result_this_period
import snout.composeapp.generated.resources.insight_result_unsupported
import snout.composeapp.generated.resources.insight_result_versus

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

        is InsightResult.Series -> SeriesView(result.series, result.format, display, chartHeight, detailed)

        is InsightResult.Funnel -> FunnelView(result, detailed)

        is InsightResult.Retention -> RetentionView(result, chartHeight, detailed)

        is InsightResult.Table -> if (result.rows.isEmpty()) {
            Note(stringResource(Res.string.insight_result_empty))
        } else {
            ExpandableRows(items = result.rows, total = result.totalRows, expandable = detailed) { rows ->
                DataTable(columns = result.columns, rows = rows)
            }
        }
    }
}

@Composable
private fun SeriesView(
    all: List<TrendSeries>,
    format: ValueFormat,
    display: InsightDisplay,
    chartHeight: Dp,
    detailed: Boolean,
) {
    if (all.isEmpty() || all.all { it.values.isEmpty() && it.aggregatedValue == null }) {
        Note(stringResource(Res.string.insight_result_empty))
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
                    Note(stringResource(Res.string.insight_result_previous_lines))
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

            InsightDisplay.Number -> NumberView(ranked, formatter, detailed)

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

@Composable
private fun NumberView(series: List<TrendSeries>, formatter: (Double) -> String, expandable: Boolean) {
    val single = series.singleOrNull()
    if (single != null) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            BoldNumber(value = formatter(single.total), label = single.label.ifEmpty { null })
            single.previous?.let { previous ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xSmall),
                ) {
                    single.changePercent?.let { TrendChip(changePercent = it) }
                    Text(
                        text = stringResource(Res.string.insight_result_versus, formatter(previous.total)),
                        style = SnoutTheme.typography.bodySmall,
                        color = SnoutTheme.colors.onSurfaceVariant,
                    )
                }
            }
        }
        return
    }
    ExpandableRows(items = series, expandable = expandable) { rows ->
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            rows.forEach { s ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                ) {
                    Text(
                        text = s.label,
                        style = SnoutTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    s.changePercent?.let { TrendChip(changePercent = it) }
                    Text(text = formatter(s.total), style = SnoutTheme.typography.headlineMedium)
                }
            }
        }
    }
}

/**
 * The first [MaxListRows] of [items], then either a static "and N more" or, when [expandable], a toggle to show
 * them all. [total] counts rows PostHog had beyond [items]; those can only be reported, not shown.
 */
@Composable
private fun <T> ExpandableRows(
    items: List<T>,
    expandable: Boolean,
    total: Int = items.size,
    content: @Composable (List<T>) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val shown = if (expandable && expanded) items else items.take(MaxListRows)
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xxSmall)) {
        content(shown)
        if (expandable && items.size > MaxListRows) {
            TextButton(onClick = { expanded = !expanded }) {
                Text(
                    if (expanded) {
                        stringResource(Res.string.insight_result_show_fewer)
                    } else {
                        stringResource(Res.string.insight_result_show_all, items.size)
                    },
                )
            }
        }
        MoreNote(Res.string.insight_result_more_rows, total - (if (expandable) items.size else shown.size))
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

@Composable
private fun MoreNote(text: StringResource, count: Int) {
    if (count > 0) Note(stringResource(text, count))
}

@Composable
private fun FunnelView(result: InsightResult.Funnel, detailed: Boolean) {
    if (result.steps.isEmpty()) {
        Note(stringResource(Res.string.insight_result_empty))
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

@Composable
private fun Note(text: String) {
    Text(text = text, style = SnoutTheme.typography.bodyMedium, color = SnoutTheme.colors.onSurfaceVariant)
}
