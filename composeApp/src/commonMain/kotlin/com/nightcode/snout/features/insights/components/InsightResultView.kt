package com.nightcode.snout.features.insights.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.charts.DataTable
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.insights.InsightDisplay
import com.nightcode.snout.shared.domain.model.insights.InsightResult
import com.nightcode.snout.shared.domain.model.insights.TestSeriesResult
import com.nightcode.snout.shared.domain.model.insights.TrendSeries
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.insight_result_empty
import snout.composeapp.generated.resources.insight_result_more_rows
import snout.composeapp.generated.resources.insight_result_show_all
import snout.composeapp.generated.resources.insight_result_show_fewer
import snout.composeapp.generated.resources.insight_result_unsupported

/** Ranked lists and tables can take more rows before the rest collapses into "and N more". */
private const val MaxListRows = 10

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
        InsightResult.Unsupported -> ResultNote(stringResource(Res.string.insight_result_unsupported))

        is InsightResult.Series -> SeriesResultView(result.series, result.format, display, chartHeight, detailed)

        is InsightResult.Funnel -> FunnelResultView(result, detailed)

        is InsightResult.Retention -> RetentionResultView(result, chartHeight, detailed)

        is InsightResult.Table -> if (result.rows.isEmpty()) {
            ResultNote(stringResource(Res.string.insight_result_empty))
        } else {
            ExpandableRows(items = result.rows, total = result.totalRows, expandable = detailed) { rows ->
                DataTable(columns = result.columns, rows = rows)
            }
        }
    }
}

/**
 * The first [MaxListRows] of [items], then either a static "and N more" or, when [expandable], a toggle to show
 * them all. [total] counts rows PostHog had beyond [items]; those can only be reported, not shown.
 */
@Composable
internal fun <T> ExpandableRows(
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
internal fun MoreNote(text: StringResource, count: Int) {
    if (count > 0) ResultNote(stringResource(text, count))
}

@Composable
internal fun ResultNote(text: String) {
    Text(text = text, style = SnoutTheme.typography.bodyMedium, color = SnoutTheme.colors.onSurfaceVariant)
}

@Composable
private fun ResultPreview(result: InsightResult, display: InsightDisplay) = Column(Modifier.padding(Spacing.large)) {
    InsightResultView(result = result, display = display)
}

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun InsightResultViewBarPreview() = ResultPreview(TestSeriesResult, InsightDisplay.Bar)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun InsightResultViewPiePreview() = ResultPreview(TestSeriesResult, InsightDisplay.Pie)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun InsightResultViewNumberPreview() = ResultPreview(
    result = InsightResult.Series(
        listOf(
            TrendSeries(
                label = "Weekly active users",
                values = emptyList(),
                periods = emptyList(),
                aggregatedValue = 4820.0,
                previous = TrendSeries("Weekly active users", emptyList(), emptyList(), aggregatedValue = 4310.0),
            ),
        ),
    ),
    display = InsightDisplay.Number,
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun InsightResultViewTablePreview() = ResultPreview(
    result = InsightResult.Table(
        columns = listOf("event", "count", "users"),
        rows = listOf(
            listOf("\$pageview", "48210", "9120"),
            listOf("\$autocapture", "21877", "6034"),
            listOf("sign up", "1312", "1290"),
        ),
        totalRows = 14,
    ),
    display = InsightDisplay.Table,
)
