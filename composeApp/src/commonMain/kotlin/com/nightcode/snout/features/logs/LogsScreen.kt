package com.nightcode.snout.features.logs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.format.formatCompact
import com.nightcode.snout.common.format.formatPercent
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.designsystem.atoms.StatusTone
import com.nightcode.snout.designsystem.atoms.Tag
import com.nightcode.snout.designsystem.charts.BarSegment
import com.nightcode.snout.designsystem.charts.ProportionBar
import com.nightcode.snout.designsystem.charts.Sparkline
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.molecules.FilterMenuChip
import com.nightcode.snout.designsystem.molecules.MenuOption
import com.nightcode.snout.designsystem.molecules.MetaText
import com.nightcode.snout.designsystem.molecules.Placeholder
import com.nightcode.snout.designsystem.organisms.CachedStateContainer
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.features.logs.components.LogsUnavailablePlaceholder
import com.nightcode.snout.features.logs.components.label
import com.nightcode.snout.features.logs.components.volumeColor
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.logs.LogLevelCounts
import com.nightcode.snout.shared.domain.model.logs.LogService
import com.nightcode.snout.shared.domain.model.logs.LogSeverity
import com.nightcode.snout.shared.domain.model.logs.LogsRange
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_subject_24dp
import snout.composeapp.generated.resources.logs_count
import snout.composeapp.generated.resources.logs_error_rate
import snout.composeapp.generated.resources.logs_services_empty_subtitle
import snout.composeapp.generated.resources.logs_services_empty_title
import snout.composeapp.generated.resources.placeholder_nothing_found_retry

/** From one error in a hundred logs, a service's error rate is worth a warning colour. */
private const val NoisyErrorRate = 0.01

@Composable
internal fun LogsScreen(
    state: LogsUiState,
    onRangeSelected: (LogsRange) -> Unit,
    onRefresh: () -> Unit,
    onOpenService: (service: String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    Column(modifier = Modifier.fillMaxSize()) {
        FilterMenuChip(
            label = stringResource(state.range.label),
            options = LogsRange.entries.map { MenuOption(it, stringResource(it.label)) },
            selected = state.range,
            onSelect = onRangeSelected,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        val services = state.services
        if (services is CachedUiState.Error && services.error == DataError.NotFound) {
            LogsUnavailablePlaceholder(onRetry = onRefresh, modifier = Modifier.fillMaxSize())
            return@Column
        }
        CachedStateContainer(
            state = services,
            onRefresh = onRefresh,
            emptyContent = {
                Placeholder(
                    icon = painterResource(Res.drawable.ic_subject_24dp),
                    title = stringResource(Res.string.logs_services_empty_title),
                    message = stringResource(Res.string.logs_services_empty_subtitle),
                    actionLabel = stringResource(Res.string.placeholder_nothing_found_retry),
                    onAction = onRefresh,
                    modifier = Modifier.fillMaxSize(),
                )
            },
        ) { list ->
            LazyColumn(contentPadding = PaddingValues(bottom = 16.dp) + contentPadding) {
                itemsIndexed(list, key = { _, service -> service.name }) { index, service ->
                    ServiceRow(
                        service = service,
                        divider = index < list.lastIndex,
                        onClick = { onOpenService(service.name) },
                    )
                }
            }
        }
    }
}

/**
 * Service name, its volume and error share, a bar of how that volume splits across levels, and a sparkline of volume
 * over the range.
 */
@Composable
private fun ServiceRow(service: LogService, divider: Boolean, onClick: () -> Unit) {
    EntityRow(
        title = service.name,
        titleStyle = SnoutTheme.code.medium,
        titleMaxLines = 1,
        divider = divider,
        onClick = onClick,
        meta = {
            MetaText(
                pluralStringResource(
                    Res.plurals.logs_count,
                    service.logCount.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                    service.logCount.toDouble().formatCompact(),
                ),
            )
            if (service.errorCount > 0) {
                Tag(
                    text = stringResource(
                        Res.string.logs_error_rate,
                        service.errorRate.formatPercent(isFraction = true),
                    ),
                    tone = if (service.errorRate >= NoisyErrorRate) StatusTone.Attention else StatusTone.Neutral,
                )
            }
        },
        trailing = { Sparkline(values = service.volume.map { it.toDouble() }) },
        supporting = service.levels?.takeIf { it.total > 0 }?.let { levels ->
            { LevelBar(levels, modifier = Modifier.padding(top = 8.dp)) }
        },
    )
}

/** Errors first so a red sliver at the start reads as trouble at a glance. */
@Composable
private fun LevelBar(levels: LogLevelCounts, modifier: Modifier = Modifier) {
    ProportionBar(
        segments = listOf(
            BarSegment(levels.error.toDouble(), LogSeverity.Error.volumeColor()),
            BarSegment(levels.warn.toDouble(), LogSeverity.Warn.volumeColor()),
            BarSegment(levels.info.toDouble(), LogSeverity.Info.volumeColor()),
            BarSegment(levels.debug.toDouble(), LogSeverity.Debug.volumeColor()),
        ),
        modifier = modifier,
    )
}

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun LogsScreenPreview() = LogsScreen(state = TestLogsUiState, onRangeSelected = {}, onRefresh = {}, onOpenService = {})

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun LogsScreenEmptyPreview() = LogsScreen(
    state = LogsUiState(services = CachedUiState.Empty(isRefreshing = false)),
    onRangeSelected = {},
    onRefresh = {},
    onOpenService = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun LogsScreenUnavailablePreview() = LogsScreen(
    state = LogsUiState(services = CachedUiState.Error(DataError.NotFound)),
    onRangeSelected = {},
    onRefresh = {},
    onOpenService = {},
)
