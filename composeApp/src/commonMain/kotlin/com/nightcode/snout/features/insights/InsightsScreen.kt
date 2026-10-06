package com.nightcode.snout.features.insights

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.format.formatRelative
import com.nightcode.snout.designsystem.atoms.LabelChip
import com.nightcode.snout.designsystem.atoms.textfields.SearchField
import com.nightcode.snout.designsystem.organisms.CachedStateContainer
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.features.insights.components.label
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.insights.InsightSummary
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.favorite_content_description
import snout.composeapp.generated.resources.ic_star_24dp
import snout.composeapp.generated.resources.ic_star_border_24dp
import snout.composeapp.generated.resources.insights_search_placeholder
import snout.composeapp.generated.resources.unfavorite_content_description

@Composable
internal fun InsightsScreen(
    state: InsightsUiState,
    snackbarHostState: SnackbarHostState,
    onQueryChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onFavoriteToggle: (InsightSummary) -> Unit,
    onOpenInsight: (insightId: Long) -> Unit,
) {
    val searchState = rememberTextFieldState(state.query)
    LaunchedEffect(searchState.text) { onQueryChange(searchState.text.toString()) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SearchField(
                state = searchState,
                placeholder = stringResource(Res.string.insights_search_placeholder),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )
            CachedStateContainer(state = state.insights, onRefresh = onRefresh) { insights ->
                LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                    items(insights, key = { it.id }) { insight ->
                        InsightRow(
                            insight = insight,
                            onClick = { onOpenInsight(insight.id) },
                            onFavoriteToggle = { onFavoriteToggle(insight) },
                        )
                    }
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
internal fun InsightRow(insight: InsightSummary, onClick: () -> Unit, onFavoriteToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = insight.name,
                style = SnoutTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            insight.description?.let {
                Text(
                    text = it,
                    style = SnoutTheme.typography.bodySmall,
                    color = SnoutTheme.colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                LabelChip(text = insight.kind.label())
                insight.lastModifiedAt?.let {
                    Text(
                        text = it.formatRelative(),
                        style = SnoutTheme.typography.labelSmall,
                        color = SnoutTheme.colors.onSurfaceVariant,
                    )
                }
            }
        }
        FavoriteIconButton(favorited = insight.favorited, onClick = onFavoriteToggle)
    }
}

@Composable
internal fun FavoriteIconButton(favorited: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(if (favorited) Res.drawable.ic_star_24dp else Res.drawable.ic_star_border_24dp),
            contentDescription = stringResource(
                if (favorited) Res.string.unfavorite_content_description else Res.string.favorite_content_description,
            ),
            tint = if (favorited) SnoutTheme.colors.primary else SnoutTheme.colors.onSurfaceVariant,
        )
    }
}

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun InsightsScreenPreview() = InsightsScreen(
    state = TestInsightsUiState,
    snackbarHostState = remember { SnackbarHostState() },
    onQueryChange = {},
    onRefresh = {},
    onFavoriteToggle = {},
    onOpenInsight = {},
)
