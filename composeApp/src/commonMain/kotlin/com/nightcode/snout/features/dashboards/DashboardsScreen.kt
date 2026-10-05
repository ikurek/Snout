package com.nightcode.snout.features.dashboards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.format.formatRelative
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.designsystem.atoms.AvatarBox
import com.nightcode.snout.designsystem.atoms.LabelChip
import com.nightcode.snout.designsystem.organisms.CachedStateContainer
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.shared.common.testdata.TestData
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AvatarBox(name = dashboard.name, size = 40.dp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (dashboard.pinned) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_push_pin_24dp),
                        contentDescription = stringResource(Res.string.pinned_content_description),
                        tint = SnoutTheme.colors.primary,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Text(
                    text = dashboard.name,
                    style = SnoutTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            dashboard.description?.let {
                Text(
                    text = it,
                    style = SnoutTheme.typography.bodySmall,
                    color = SnoutTheme.colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                dashboard.tags.take(MaxTags).forEach { LabelChip(text = it) }
                dashboard.lastAccessedAt?.let {
                    Text(
                        text = stringResource(Res.string.dashboards_last_viewed, it.formatRelative()),
                        style = SnoutTheme.typography.labelSmall,
                        color = SnoutTheme.colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
private fun DashboardsScreenPreview() = DashboardsScreen(
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
