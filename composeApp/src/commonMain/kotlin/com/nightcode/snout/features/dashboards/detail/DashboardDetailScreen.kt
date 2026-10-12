package com.nightcode.snout.features.dashboards.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.compose.bottomOnly
import com.nightcode.snout.common.compose.withoutBottom
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.designsystem.atoms.SkeletonLines
import com.nightcode.snout.designsystem.atoms.Tag
import com.nightcode.snout.designsystem.molecules.InfoBanner
import com.nightcode.snout.designsystem.molecules.InlineErrorBanner
import com.nightcode.snout.designsystem.molecules.SectionCard
import com.nightcode.snout.designsystem.organisms.CachedStateContainer
import com.nightcode.snout.designsystem.organisms.NothingFoundPlaceholder
import com.nightcode.snout.designsystem.organisms.PushedScreenScaffold
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.features.insights.components.InsightResultView
import com.nightcode.snout.features.insights.components.label
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.insights.DashboardTile
import com.nightcode.snout.shared.domain.model.insights.TestDashboard
import com.nightcode.snout.shared.domain.model.insights.TestInsightSummary
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.dashboard_tile_error
import snout.composeapp.generated.resources.dashboard_title_placeholder

@Composable
internal fun DashboardDetailScreen(
    state: DashboardDetailUiState,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onRetryTile: (insightId: Long) -> Unit,
    onOpenInsight: (insightId: Long) -> Unit,
    onOpenInPostHog: () -> Unit,
) {
    val title = (state.dashboard as? CachedUiState.Content)?.data?.name
        ?: stringResource(Res.string.dashboard_title_placeholder)
    PushedScreenScaffold(
        title = title,
        onNavigateBack = onNavigateBack,
        onOpenInPostHog = onOpenInPostHog,
    ) { innerPadding ->
        CachedStateContainer(
            state = state.dashboard,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(innerPadding.withoutBottom()),
        ) { dashboard ->
            if (dashboard.tiles.isEmpty()) {
                NothingFoundPlaceholder(onRetry = onRefresh, modifier = Modifier.fillMaxSize())
            } else {
                TileList(
                    state = state,
                    tiles = dashboard.tiles,
                    onRetryTile = onRetryTile,
                    onOpenInsight = onOpenInsight,
                    contentPadding = innerPadding.bottomOnly(),
                )
            }
        }
    }
}

@Composable
private fun TileList(
    state: DashboardDetailUiState,
    tiles: List<DashboardTile>,
    onRetryTile: (insightId: Long) -> Unit,
    onOpenInsight: (insightId: Long) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = Spacing.large, vertical = Spacing.small) + contentPadding,
        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        items(tiles, key = { it.id }) { tile ->
            when (tile) {
                is DashboardTile.TextTile -> InfoBanner(text = tile.body, showIcon = false)

                is DashboardTile.InsightTile -> InsightTileCard(
                    tile = tile,
                    status = state.tiles[tile.insight.id],
                    onRetry = { onRetryTile(tile.insight.id) },
                    onClick = { onOpenInsight(tile.insight.id) },
                )
            }
        }
    }
}

/** Insight tile: header with its type tag, then the chart; three skeleton lines while PostHog computes it. */
@Composable
private fun InsightTileCard(
    tile: DashboardTile.InsightTile,
    status: TileStatus?,
    onRetry: () -> Unit,
    onClick: () -> Unit,
) {
    SectionCard(
        title = tile.insight.name,
        subtitle = tile.insight.description,
        trailing = { Tag(text = tile.insight.kind.label()) },
        onClick = onClick,
    ) {
        val result = tile.result
        when {
            result != null -> InsightResultView(result = result, display = tile.insight.display)

            status is TileStatus.Failed -> InlineErrorBanner(
                text = stringResource(Res.string.dashboard_tile_error),
                onAction = onRetry,
            )

            else -> SkeletonLines(modifier = Modifier.padding(vertical = Spacing.large))
        }
    }
}

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun DashboardDetailScreenPreview() = DashboardDetailScreen(
    state = TestDashboardDetailUiState,
    onNavigateBack = {},
    onRefresh = {},
    onRetryTile = {},
    onOpenInsight = {},
    onOpenInPostHog = {},
)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun DashboardDetailScreenTilesPendingPreview() = DashboardDetailScreen(
    state = TestDashboardDetailUiState.copy(
        dashboard = CachedUiState.Content(
            TestDashboard.copy(
                tiles = TestDashboard.tiles.map { tile ->
                    if (tile is DashboardTile.InsightTile) tile.copy(result = null) else tile
                },
            ),
            isRefreshing = false,
        ),
        tiles = mapOf(
            TestInsightSummary.id to TileStatus.Loading,
            11L to TileStatus.Failed(DataError.RateLimited),
        ),
    ),
    onNavigateBack = {},
    onRefresh = {},
    onRetryTile = {},
    onOpenInsight = {},
    onOpenInPostHog = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun DashboardDetailScreenErrorPreview() = DashboardDetailScreen(
    state = DashboardDetailUiState(dashboard = CachedUiState.Error(DataError.NotFound)),
    onNavigateBack = {},
    onRefresh = {},
    onRetryTile = {},
    onOpenInsight = {},
    onOpenInPostHog = {},
)
