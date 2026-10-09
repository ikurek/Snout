package com.nightcode.snout.features.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.nightcode.snout.designsystem.atoms.chips.ChoiceChip
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
import com.nightcode.snout.shared.domain.model.analytics.MetricTrend
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetric
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKind
import com.nightcode.snout.shared.domain.model.analytics.TestAnalyticsSnapshot
import com.nightcode.snout.shared.domain.model.analytics.TrendMetric
import com.nightcode.snout.shared.domain.model.common.DataError
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.analytics_breakdown_views
import snout.composeapp.generated.resources.analytics_metric_previous
import snout.composeapp.generated.resources.analytics_no_data
import snout.composeapp.generated.resources.analytics_trend_current
import snout.composeapp.generated.resources.analytics_trend_previous
import snout.composeapp.generated.resources.analytics_trend_title

private const val SkeletonStatRows = 2
private const val SkeletonLabelWidth = 0.5f
private const val SkeletonValueWidth = 0.6f
private const val SkeletonChipWidth = 0.7f

@Composable
internal fun AnalyticsScreen(
    state: AnalyticsUiState,
    onRangeSelected: (AnalyticsRange) -> Unit,
    onRefresh: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
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
            SnapshotContent(snapshot, contentPadding)
        }
    }
}

@Composable
private fun SnapshotContent(snapshot: AnalyticsSnapshot, contentPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OverviewGrid(snapshot.overview)
        if (snapshot.trends.isNotEmpty()) TrendCard(snapshot.trends)
        // Web projects have no screens and mobile projects have no pages; only show what has data.
        snapshot.breakdowns.filter { it.rows.isNotEmpty() }.forEach { BreakdownCard(it) }
        VerticalSpacer(8.dp)
    }
}

/**
 * Two columns of stat cards with 8dp gaps; an odd last card keeps one column. Cards in a row share the taller
 * one's height, so a metric without a comparison (bounce rate with no sessions) doesn't end up shorter.
 */
@Composable
private fun OverviewGrid(metrics: List<OverviewMetric>) {
    val previousLabel = stringResource(Res.string.analytics_metric_previous)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        metrics.chunked(2).forEach { pair ->
            Row(modifier = Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { metric ->
                    StatCard(
                        label = metric.label(),
                        value = metric.value?.let { metric.kind.format(it) } ?: "–",
                        changePercent = metric.changePercent,
                        lowerIsBetter = metric.isIncreaseBad,
                        comparison = metric.previous?.let { "$previousLabel ${metric.kind.format(it)}" },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
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

/** One chart for visitors, views or sessions, with the preceding period dashed behind it. */
@Composable
private fun TrendCard(trends: List<MetricTrend>) {
    var selected by rememberSaveable { mutableStateOf(TrendMetric.Visitors) }
    val trend = trends.firstOrNull { it.metric == selected } ?: trends.first()
    SectionCard(title = stringResource(Res.string.analytics_trend_title, trend.metric.label())) {
        if (trends.size > 1) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                trends.forEach { option ->
                    ChoiceChip(
                        text = option.metric.label(),
                        selected = option.metric == trend.metric,
                        onClick = { selected = option.metric },
                    )
                }
            }
        }
        if (trend.current.points.isEmpty()) {
            EmptyNote()
        } else {
            val previous = trend.previous?.takeIf { it.points.isNotEmpty() }
            LineChart(
                series = listOfNotNull(
                    ChartSeries(
                        name = stringResource(Res.string.analytics_trend_current),
                        values = trend.current.points.map { it.value },
                        color = chartColor(0),
                    ),
                    previous?.let {
                        ChartSeries(
                            name = stringResource(Res.string.analytics_trend_previous),
                            values = it.points.map { point -> point.value },
                            color = SnoutTheme.colors.onSurfaceVariant,
                            dashed = true,
                            filled = false,
                        )
                    },
                ),
                xLabels = trend.current.points.map { it.label.formatChartLabel() },
                fill = true,
            )
        }
    }
}

@Composable
private fun BreakdownCard(table: BreakdownTable) {
    SectionCard(title = table.breakdown.label()) {
        RankedBarList(
            labelIsCode = table.breakdown.labelIsCode,
            rows = table.rows.map { row ->
                RankedBarRow(label = row.label, value = row.visitors, secondary = row.views?.let { viewsLabel(it) })
            },
        )
    }
}

/** "1 view", "47 views", "1.2k views"; only an exact single view takes the singular. */
@Composable
private fun viewsLabel(views: Double): String =
    pluralStringResource(Res.plurals.analytics_breakdown_views, if (views == 1.0) 1 else 2, views.formatCompact())

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
