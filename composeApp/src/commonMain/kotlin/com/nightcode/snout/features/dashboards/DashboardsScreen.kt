package com.nightcode.snout.features.dashboards

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.format.formatRelative
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.designsystem.LabelChip
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.molecules.MetaText
import com.nightcode.snout.designsystem.organisms.CachedStateContainer
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.insights.DashboardSummary
import com.nightcode.snout.shared.domain.model.insights.TestDashboardSummary
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.dashboards_last_viewed
import snout.composeapp.generated.resources.ic_push_pin_24dp
import snout.composeapp.generated.resources.pinned_content_description

private const val MaxTags = 3

@Composable
internal fun DashboardsScreen(
    state: CachedUiState<List<DashboardSummary>>,
    onRefresh: () -> Unit,
    onOpenDashboard: (dashboardId: Long) -> Unit,
) {
    CachedStateContainer(state = state, onRefresh = onRefresh, modifier = Modifier.fillMaxSize()) { dashboards ->
        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
            items(dashboards, key = { it.id }) { dashboard ->
                DashboardRow(dashboard = dashboard, onClick = { onOpenDashboard(dashboard.id) })
            }
        }
    }
}

@Composable
private fun DashboardRow(dashboard: DashboardSummary, onClick: () -> Unit) {
    EntityRow(
        title = dashboard.name,
        subtitle = dashboard.description,
        onClick = onClick,
        meta = {
            dashboard.tags.take(MaxTags).forEach { LabelChip(text = it) }
            dashboard.lastAccessedAt?.let {
                MetaText(stringResource(Res.string.dashboards_last_viewed, it.formatRelative()))
            }
        },
        trailing = if (dashboard.pinned) {
            {
                Icon(
                    painter = painterResource(Res.drawable.ic_push_pin_24dp),
                    contentDescription = stringResource(Res.string.pinned_content_description),
                    tint = SnoutTheme.colors.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        } else {
            null
        },
    )
}

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun DashboardsScreenPreview() = DashboardsScreen(
    state = CachedUiState.Content(
        listOf(
            TestDashboardSummary,
            TestDashboardSummary.copy(id = 2, name = "Marketing", pinned = false, tags = emptyList()),
        ),
        isRefreshing = false,
    ),
    onRefresh = {},
    onOpenDashboard = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun DashboardsScreenEmptyPreview() = DashboardsScreen(
    state = CachedUiState.Empty(isRefreshing = false),
    onRefresh = {},
    onOpenDashboard = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun DashboardsScreenErrorPreview() = DashboardsScreen(
    state = CachedUiState.Error(DataError.ConnectionError),
    onRefresh = {},
    onOpenDashboard = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun DashboardsScreenLoadingPreview() = DashboardsScreen(
    state = CachedUiState.Loading,
    onRefresh = {},
    onOpenDashboard = {},
)
