package com.nightcode.snout.features.dashboards.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.designsystem.LabelChip
import com.nightcode.snout.designsystem.atoms.buttons.NavigationBackIconButton
import com.nightcode.snout.designsystem.atoms.buttons.OpenInPostHogIconButton
import com.nightcode.snout.designsystem.InlineError
import com.nightcode.snout.designsystem.molecules.SectionCard
import com.nightcode.snout.designsystem.organisms.CachedStateContainer
import com.nightcode.snout.designsystem.organisms.NothingFoundPlaceholder
import com.nightcode.snout.designsystem.tokens.SnoutTheme
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

@OptIn(ExperimentalMaterial3Api::class)
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { NavigationBackIconButton(onClick = onNavigateBack) },
                actions = { OpenInPostHogIconButton(onClick = onOpenInPostHog) },
            )
        },
    ) { innerPadding ->
        CachedStateContainer(
            state = state.dashboard,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) { dashboard ->
            if (dashboard.tiles.isEmpty()) {
                NothingFoundPlaceholder(onRetry = onRefresh, modifier = Modifier.fillMaxSize())
            } else {
                TileList(
                    state = state,
                    tiles = dashboard.tiles,
                    onRetryTile = onRetryTile,
                    onOpenInsight = onOpenInsight,
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
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(tiles, key = { it.id }) { tile ->
            when (tile) {
                is DashboardTile.TextTile -> TextTileCard(tile)

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

@Composable
private fun TextTileCard(tile: DashboardTile.TextTile) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = SnoutTheme.shapes.medium,
        color = SnoutTheme.colors.surfaceContainerLow,
    ) {
        Text(text = tile.body, style = SnoutTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp))
    }
}

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
        trailing = { LabelChip(text = tile.insight.kind.label()) },
        onClick = onClick,
    ) {
        val result = tile.result
        when {
            result != null -> InsightResultView(result = result, display = tile.insight.display)

            status is TileStatus.Failed -> InlineError(
                text = stringResource(Res.string.dashboard_tile_error),
                onRetry = onRetry,
            )

            else -> Box(modifier = Modifier.fillMaxWidth().height(96.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
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
