package com.nightcode.snout.features.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
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
import com.nightcode.snout.designsystem.atoms.SkeletonBlock
import com.nightcode.snout.designsystem.atoms.SkeletonLines
import com.nightcode.snout.designsystem.atoms.VerticalSpacer
import com.nightcode.snout.designsystem.charts.ChartSeries
import com.nightcode.snout.designsystem.charts.LineChart
import com.nightcode.snout.designsystem.charts.RankedBarList
import com.nightcode.snout.designsystem.charts.RankedBarRow
import com.nightcode.snout.designsystem.charts.chartColor
import com.nightcode.snout.designsystem.molecules.SectionCard
import com.nightcode.snout.designsystem.molecules.SegmentedControl
import com.nightcode.snout.designsystem.molecules.StatCard
import com.nightcode.snout.designsystem.organisms.CachedStateContainer
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsSnapshot
import com.nightcode.snout.shared.domain.model.analytics.BreakdownTable
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetric
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKind
import com.nightcode.snout.shared.domain.model.analytics.TestAnalyticsSnapshot
import com.nightcode.snout.shared.domain.model.analytics.TimeSeries
import com.nightcode.snout.shared.domain.model.common.DataError
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.analytics_breakdown_views
import snout.composeapp.generated.resources.analytics_metric_previous
import snout.composeapp.generated.resources.analytics_metric_visitors
import snout.composeapp.generated.resources.analytics_no_data
import snout.composeapp.generated.resources.analytics_visitors_trend

private const val SkeletonStatRows = 2
private const val SkeletonLabelWidth = 0.5f
private const val SkeletonValueWidth = 0.6f
private const val SkeletonChipWidth = 0.7f

@Composable
internal fun AnalyticsScreen(
    state: AnalyticsUiState,
    onRangeSelected: (AnalyticsRange) -> Unit,
    onRefresh: () -> Unit,
) {
    val rangeLabels = AnalyticsRange.entries.associateWith { it.label() }
    Column(modifier = Modifier.fillMaxSize()) {
        SegmentedControl(
            options = AnalyticsRange.entries,
            selected = state.range,
            onSelect = onRangeSelected,
            label = { rangeLabels.getValue(it) },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        CachedStateContainer(
            state = state.snapshot,
            onRefresh = onRefresh,
            loadingContent = { SnapshotSkeleton() },
        ) { snapshot ->
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

/** Two columns of stat cards with 8dp gaps; an odd last card keeps one column. */
@Composable
private fun OverviewGrid(metrics: List<OverviewMetric>) {
    val previousLabel = stringResource(Res.string.analytics_metric_previous)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        metrics.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { metric ->
                    StatCard(
                        label = metric.label(),
                        value = metric.value?.let { metric.kind.format(it) } ?: "–",
                        changePercent = metric.changePercent,
                        lowerIsBetter = metric.isIncreaseBad,
                        comparison = metric.previous?.let { "$previousLabel ${metric.kind.format(it)}" },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

/** Skeleton shaped like the stat grid and the chart card so the layout does not jump when data arrives. */
@Composable
private fun SnapshotSkeleton() {
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(SkeletonStatRows) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonStatCard(modifier = Modifier.weight(1f))
                SkeletonStatCard(modifier = Modifier.weight(1f))
            }
        }
        VerticalSpacer(4.dp)
        Surface(shape = SnoutTheme.shapes.large, color = SnoutTheme.colors.surfaceContainerLow) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SkeletonBlock(Modifier.fillMaxWidth(SkeletonLabelWidth).height(16.dp))
                SkeletonLines()
            }
        }
    }
}

@Composable
private fun SkeletonStatCard(modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = SnoutTheme.shapes.medium, color = SnoutTheme.colors.surfaceContainerLow) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonBlock(Modifier.fillMaxWidth(SkeletonLabelWidth).height(12.dp))
            SkeletonBlock(Modifier.fillMaxWidth(SkeletonValueWidth).height(28.dp))
            SkeletonBlock(Modifier.fillMaxWidth(SkeletonChipWidth).height(16.dp))
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
        style = SnoutTheme.typography.bodyMedium,
        color = SnoutTheme.colors.onSurfaceVariant,
    )
}

private fun OverviewMetricKind.format(value: Double): String = when (this) {
    OverviewMetricKind.Count -> value.formatCompact()
    OverviewMetricKind.DurationSeconds -> value.formatDurationSeconds()
    OverviewMetricKind.Percentage -> value.formatPercent(isFraction = value <= 1.0)
}

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun AnalyticsScreenPreview() = AnalyticsScreen(
    state = TestAnalyticsUiState,
    onRangeSelected = {},
    onRefresh = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun AnalyticsScreenErrorPreview() = AnalyticsScreen(
    state = AnalyticsUiState(snapshot = CachedUiState.Error(DataError.Forbidden)),
    onRangeSelected = {},
    onRefresh = {},
)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun AnalyticsScreenRefreshErrorPreview() = AnalyticsScreen(
    state = AnalyticsUiState(
        snapshot = CachedUiState.Content(
            TestAnalyticsSnapshot,
            isRefreshing = false,
            refreshError = DataError.ConnectionError,
        ),
    ),
    onRangeSelected = {},
    onRefresh = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun AnalyticsScreenLoadingPreview() = AnalyticsScreen(
    state = AnalyticsUiState(snapshot = CachedUiState.Loading),
    onRangeSelected = {},
    onRefresh = {},
)
