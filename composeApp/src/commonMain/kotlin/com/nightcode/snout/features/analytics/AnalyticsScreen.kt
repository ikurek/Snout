package com.nightcode.snout.features.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.format.formatChartLabel
import com.nightcode.snout.common.format.formatCompact
import com.nightcode.snout.common.format.formatDurationSeconds
import com.nightcode.snout.common.format.formatPercent
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.designsystem.atoms.VerticalSpacer
import com.nightcode.snout.designsystem.charts.ChartSeries
import com.nightcode.snout.designsystem.charts.LineChart
import com.nightcode.snout.designsystem.charts.RankedBarList
import com.nightcode.snout.designsystem.charts.RankedBarRow
import com.nightcode.snout.designsystem.charts.chartColor
import com.nightcode.snout.designsystem.molecules.MetricCard
import com.nightcode.snout.designsystem.molecules.SectionCard
import com.nightcode.snout.designsystem.molecules.SegmentedChoice
import com.nightcode.snout.designsystem.organisms.CachedStateContainer
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsSnapshot
import com.nightcode.snout.shared.domain.model.analytics.BreakdownTable
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetric
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKey
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKind
import com.nightcode.snout.shared.domain.model.analytics.TimeSeries
import com.nightcode.snout.shared.domain.model.analytics.WebBreakdown
import com.nightcode.snout.shared.domain.model.common.DataError
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.analytics_breakdown_country
import snout.composeapp.generated.resources.analytics_breakdown_device
import snout.composeapp.generated.resources.analytics_breakdown_pages
import snout.composeapp.generated.resources.analytics_breakdown_screens
import snout.composeapp.generated.resources.analytics_breakdown_sources
import snout.composeapp.generated.resources.analytics_breakdown_views
import snout.composeapp.generated.resources.analytics_metric_bounce_rate
import snout.composeapp.generated.resources.analytics_metric_previous
import snout.composeapp.generated.resources.analytics_metric_session_duration
import snout.composeapp.generated.resources.analytics_metric_sessions
import snout.composeapp.generated.resources.analytics_metric_views
import snout.composeapp.generated.resources.analytics_metric_visitors
import snout.composeapp.generated.resources.analytics_no_data
import snout.composeapp.generated.resources.analytics_range_30d
import snout.composeapp.generated.resources.analytics_range_7d
import snout.composeapp.generated.resources.analytics_range_90d
import snout.composeapp.generated.resources.analytics_range_today
import snout.composeapp.generated.resources.analytics_visitors_trend

@Composable
internal fun AnalyticsScreen(
    state: AnalyticsUiState,
    onRangeSelected: (AnalyticsRange) -> Unit,
    onRefresh: () -> Unit,
) {
    val rangeLabels = AnalyticsRange.entries.associateWith { it.label() }
    Column(modifier = Modifier.fillMaxSize()) {
        SegmentedChoice(
            options = AnalyticsRange.entries,
            selected = state.range,
            onSelect = onRangeSelected,
            label = { rangeLabels.getValue(it) },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        CachedStateContainer(state = state.snapshot, onRefresh = onRefresh) { snapshot ->
            SnapshotContent(snapshot)
        }
    }
}

@Composable
private fun SnapshotContent(snapshot: AnalyticsSnapshot) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OverviewGrid(snapshot.overview)
        snapshot.visitorsTrend?.let { VisitorsTrendCard(it) }
        // Web projects have no screens and mobile projects have no pages; only show what has data.
        snapshot.breakdowns.filter { it.rows.isNotEmpty() }.forEach { BreakdownCard(it) }
        VerticalSpacer(8.dp)
    }
}

@Composable
private fun OverviewGrid(metrics: List<OverviewMetric>) {
    val previousLabel = stringResource(Res.string.analytics_metric_previous)
    metrics.chunked(2).forEach { pair ->
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            pair.forEach { metric ->
                MetricCard(
                    label = metric.label(),
                    value = metric.value?.let { metric.kind.format(it) } ?: "–",
                    changePercent = metric.changePercent,
                    isIncreaseBad = metric.isIncreaseBad,
                    secondary = metric.previous?.let { "$previousLabel ${metric.kind.format(it)}" },
                    modifier = Modifier.weight(1f),
                )
            }
            if (pair.size == 1) Column(modifier = Modifier.weight(1f)) {}
        }
    }
}

@Composable
private fun VisitorsTrendCard(trend: TimeSeries) {
    SectionCard(title = stringResource(Res.string.analytics_visitors_trend)) {
        if (trend.points.isEmpty()) {
            EmptyNote()
        } else {
            LineChart(
                series = listOf(
                    ChartSeries(
                        name = stringResource(Res.string.analytics_metric_visitors),
                        values = trend.points.map { it.value },
                        color = chartColor(0),
                    ),
                ),
                xLabels = trend.points.map { it.label.formatChartLabel() },
            )
        }
    }
}

@Composable
private fun BreakdownCard(table: BreakdownTable) {
    val viewsLabel = stringResource(Res.string.analytics_breakdown_views)
    SectionCard(title = table.breakdown.label()) {
        RankedBarList(
            rows = table.rows.map { row ->
                RankedBarRow(
                    label = row.label,
                    value = row.visitors,
                    secondary = row.views?.let { "${it.formatCompact()} $viewsLabel" },
                )
            },
        )
    }
}

@Composable
private fun EmptyNote() {
    Text(
        text = stringResource(Res.string.analytics_no_data),
        style = SnoutTheme.typography.bodySmall,
        color = SnoutTheme.colors.onSurfaceVariant,
    )
}

private fun OverviewMetricKind.format(value: Double): String = when (this) {
    OverviewMetricKind.Count -> value.formatCompact()
    OverviewMetricKind.DurationSeconds -> value.formatDurationSeconds()
    OverviewMetricKind.Percentage -> value.formatPercent(isFraction = value <= 1.0)
}

@Composable
private fun OverviewMetric.label(): String = when (key) {
    OverviewMetricKey.Visitors -> stringResource(Res.string.analytics_metric_visitors)
    OverviewMetricKey.Views -> stringResource(Res.string.analytics_metric_views)
    OverviewMetricKey.Sessions -> stringResource(Res.string.analytics_metric_sessions)
    OverviewMetricKey.SessionDuration -> stringResource(Res.string.analytics_metric_session_duration)
    OverviewMetricKey.BounceRate -> stringResource(Res.string.analytics_metric_bounce_rate)
    OverviewMetricKey.Other -> label.replaceFirstChar { it.uppercase() }
}

@Composable
private fun WebBreakdown.label(): String = stringResource(
    when (this) {
        WebBreakdown.Page -> Res.string.analytics_breakdown_pages
        WebBreakdown.Screen -> Res.string.analytics_breakdown_screens
        WebBreakdown.ReferringDomain -> Res.string.analytics_breakdown_sources
        WebBreakdown.Country -> Res.string.analytics_breakdown_country
        WebBreakdown.DeviceType -> Res.string.analytics_breakdown_device
    },
)

@Composable
private fun AnalyticsRange.label(): String = stringResource(
    when (this) {
        AnalyticsRange.Today -> Res.string.analytics_range_today
        AnalyticsRange.Last7Days -> Res.string.analytics_range_7d
        AnalyticsRange.Last30Days -> Res.string.analytics_range_30d
        AnalyticsRange.Last90Days -> Res.string.analytics_range_90d
    },
)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
private fun AnalyticsScreenPreview() = AnalyticsScreen(
    state = TestAnalyticsUiState,
    onRangeSelected = {},
    onRefresh = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
private fun AnalyticsScreenErrorPreview() = AnalyticsScreen(
    state = AnalyticsUiState(snapshot = CachedUiState.Error(DataError.Forbidden)),
    onRangeSelected = {},
    onRefresh = {},
)
