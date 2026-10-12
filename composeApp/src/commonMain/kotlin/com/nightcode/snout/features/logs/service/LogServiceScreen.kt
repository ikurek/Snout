package com.nightcode.snout.features.logs.service

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.compose.bottomOnly
import com.nightcode.snout.common.compose.withoutBottom
import com.nightcode.snout.common.format.formatChartTime
import com.nightcode.snout.common.format.formatLogTime
import com.nightcode.snout.common.state.RefreshableUiState
import com.nightcode.snout.designsystem.atoms.SectionLabel
import com.nightcode.snout.designsystem.atoms.SkeletonRows
import com.nightcode.snout.designsystem.atoms.textfields.SearchField
import com.nightcode.snout.designsystem.charts.BarChart
import com.nightcode.snout.designsystem.charts.ChartSeries
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.molecules.FilterMenuChip
import com.nightcode.snout.designsystem.molecules.InlineErrorBanner
import com.nightcode.snout.designsystem.molecules.MenuOption
import com.nightcode.snout.designsystem.molecules.MetaText
import com.nightcode.snout.designsystem.molecules.Placeholder
import com.nightcode.snout.designsystem.organisms.NoSearchResultsPlaceholder
import com.nightcode.snout.designsystem.organisms.PushedScreenScaffold
import com.nightcode.snout.designsystem.organisms.RefreshableStateContainer
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.features.logs.components.LogSeverityTag
import com.nightcode.snout.features.logs.components.LogsUnavailablePlaceholder
import com.nightcode.snout.features.logs.components.label
import com.nightcode.snout.features.logs.components.volumeColor
import com.nightcode.snout.features.logs.components.volumeLabel
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.logs.LogEntry
import com.nightcode.snout.shared.domain.model.logs.LogSeverityFilter
import com.nightcode.snout.shared.domain.model.logs.LogVolume
import com.nightcode.snout.shared.domain.model.logs.LogsRange
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_subject_24dp
import snout.composeapp.generated.resources.logs_empty_subtitle
import snout.composeapp.generated.resources.logs_empty_title
import snout.composeapp.generated.resources.logs_load_more_failed
import snout.composeapp.generated.resources.logs_load_more_retry
import snout.composeapp.generated.resources.logs_search_empty_title
import snout.composeapp.generated.resources.logs_search_placeholder
import snout.composeapp.generated.resources.logs_volume_section
import snout.composeapp.generated.resources.placeholder_nothing_found_retry

/** Log bodies can be stack traces; three lines tell entries apart, the sheet shows the rest. */
private const val BodyPreviewLines = 3
private const val LoadingMoreRows = 2

@Composable
internal fun LogServiceScreen(
    service: String,
    range: LogsRange,
    state: LogServiceUiState,
    onNavigateBack: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onSeverityChanged: (LogSeverityFilter) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onOpenEntry: (LogEntry) -> Unit,
    onOpenInPostHog: () -> Unit,
) {
    val searchState = rememberTextFieldState(state.query)
    LaunchedEffect(searchState.text) { onQueryChanged(searchState.text.toString()) }

    PushedScreenScaffold(
        title = service,
        onNavigateBack = onNavigateBack,
        onOpenInPostHog = onOpenInPostHog,
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding.withoutBottom())) {
            LogFilters(searchState = searchState, severity = state.severity, onSeverityChanged = onSeverityChanged)
            RefreshableStateContainer(
                state = state.logs,
                onRefresh = onRefresh,
                notFoundContent = {
                    LogsUnavailablePlaceholder(onRetry = onRefresh, modifier = Modifier.fillMaxSize())
                },
                emptyContent = {
                    if (state.query.isBlank()) {
                        Placeholder(
                            icon = painterResource(Res.drawable.ic_subject_24dp),
                            title = stringResource(Res.string.logs_empty_title),
                            message = stringResource(Res.string.logs_empty_subtitle),
                            actionLabel = stringResource(Res.string.placeholder_nothing_found_retry),
                            onAction = onRefresh,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        NoSearchResultsPlaceholder(
                            title = stringResource(Res.string.logs_search_empty_title),
                            onClearSearch = { searchState.clearText() },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                },
            ) { entries ->
                LazyColumn(contentPadding = PaddingValues(bottom = Spacing.large) + innerPadding.bottomOnly()) {
                    state.volume?.let { volume ->
                        item(key = "volume") { VolumeChart(volume, includeDate = range != LogsRange.LastHour) }
                    }
                    itemsIndexed(entries, key = { _, entry -> entry.id }) { index, entry ->
                        LogRow(entry = entry, divider = index < entries.lastIndex, onClick = { onOpenEntry(entry) })
                    }
                    item(key = "load-more") { LoadMoreFooter(status = state.loadMore, onLoadMore = onLoadMore) }
                }
            }
        }
    }
}

/** Search over messages and the level dropdown, above the chart and the lines. */
@Composable
private fun LogFilters(
    searchState: TextFieldState,
    severity: LogSeverityFilter,
    onSeverityChanged: (LogSeverityFilter) -> Unit,
) {
    SearchField(
        state = searchState,
        placeholder = stringResource(Res.string.logs_search_placeholder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.large)
            .padding(top = Spacing.small, bottom = Spacing.xxSmall),
    )
    FilterMenuChip(
        label = stringResource(severity.label),
        options = LogSeverityFilter.entries.map { MenuOption(it, stringResource(it.label)) },
        selected = severity,
        onSelect = onSeverityChanged,
        active = severity != LogSeverityFilter.All,
        modifier = Modifier.padding(horizontal = Spacing.large, vertical = Spacing.xxSmall),
    )
}

/** Log volume per bucket, levels stacked with errors at the baseline; only levels that logged anything appear. */
@Composable
private fun VolumeChart(volume: LogVolume, includeDate: Boolean) {
    val series = volume.counts.filterValues { counts -> counts.any { it > 0 } }.map { (level, counts) ->
        ChartSeries(
            name = stringResource(level.volumeLabel),
            values = counts.map { it.toDouble() },
            color = level.volumeColor(),
        )
    }
    SectionLabel(stringResource(Res.string.logs_volume_section))
    BarChart(
        series = series,
        xLabels = volume.buckets.map { it.formatChartTime(includeDate) },
        stacked = true,
        showLegend = true,
        height = 140.dp,
        modifier = Modifier.padding(horizontal = Spacing.large).padding(bottom = Spacing.small),
    )
}

/** The message in monospace, then its level and time. */
@Composable
private fun LogRow(entry: LogEntry, divider: Boolean, onClick: () -> Unit) {
    EntityRow(
        title = entry.body,
        titleStyle = SnoutTheme.code.small,
        titleMaxLines = BodyPreviewLines,
        alignment = Alignment.Top,
        divider = divider,
        onClick = onClick,
        meta = {
            LogSeverityTag(entry.severity)
            MetaText(entry.timestamp.formatLogTime())
        },
    )
}

/** Composing the footer means the list reached its end, which is the cue to fetch the next page. */
@Composable
private fun LoadMoreFooter(status: LoadMoreStatus, onLoadMore: () -> Unit) {
    when (status) {
        LoadMoreStatus.Idle -> LaunchedEffect(Unit) { onLoadMore() }

        LoadMoreStatus.Loading -> SkeletonRows(count = LoadingMoreRows)

        is LoadMoreStatus.Failed -> InlineErrorBanner(
            text = stringResource(Res.string.logs_load_more_failed),
            actionLabel = stringResource(Res.string.logs_load_more_retry),
            onAction = onLoadMore,
            modifier = Modifier.padding(Spacing.large),
        )

        LoadMoreStatus.Done -> Unit
    }
}

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun LogServiceScreenPreview() = LogServiceScreen(
    service = "checkout-api",
    range = LogsRange.Last24Hours,
    state = TestLogServiceUiState,
    onNavigateBack = {},
    onQueryChanged = {},
    onSeverityChanged = {},
    onRefresh = {},
    onLoadMore = {},
    onOpenEntry = {},
    onOpenInPostHog = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun LogServiceScreenEmptyPreview() = LogServiceScreen(
    service = "checkout-api",
    range = LogsRange.Last24Hours,
    state = LogServiceUiState(
        severity = LogSeverityFilter.Errors,
        logs = RefreshableUiState.Empty(isRefreshing = false),
    ),
    onNavigateBack = {},
    onQueryChanged = {},
    onSeverityChanged = {},
    onRefresh = {},
    onLoadMore = {},
    onOpenEntry = {},
    onOpenInPostHog = {},
)
