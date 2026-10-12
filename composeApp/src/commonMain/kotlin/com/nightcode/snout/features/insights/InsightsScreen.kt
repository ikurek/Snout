package com.nightcode.snout.features.insights

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.format.formatRelative
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.designsystem.atoms.Tag
import com.nightcode.snout.designsystem.atoms.textfields.SearchField
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.molecules.MetaText
import com.nightcode.snout.designsystem.organisms.CachedStateContainer
import com.nightcode.snout.designsystem.organisms.NoSearchResultsPlaceholder
import com.nightcode.snout.designsystem.organisms.NothingFoundPlaceholder
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.features.insights.components.label
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.insights.InsightSummary
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.favorite_content_description
import snout.composeapp.generated.resources.ic_star_24dp
import snout.composeapp.generated.resources.ic_star_border_24dp
import snout.composeapp.generated.resources.insights_search_empty_title
import snout.composeapp.generated.resources.insights_search_placeholder
import snout.composeapp.generated.resources.unfavorite_content_description

@Composable
internal fun InsightsScreen(
    state: InsightsUiState,
    snackbarHostState: SnackbarHostState,
    onQueryChanged: (String) -> Unit,
    onRefresh: () -> Unit,
    onToggleFavorite: (InsightSummary) -> Unit,
    onOpenInsight: (insightId: Long) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val searchState = rememberTextFieldState(state.query)
    LaunchedEffect(searchState.text) { onQueryChanged(searchState.text.toString()) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SearchField(
                state = searchState,
                placeholder = stringResource(Res.string.insights_search_placeholder),
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.large, vertical = Spacing.small),
            )
            CachedStateContainer(
                state = state.insights,
                onRefresh = onRefresh,
                emptyContent = {
                    if (state.query.isBlank()) {
                        NothingFoundPlaceholder(onRetry = onRefresh, modifier = Modifier.fillMaxSize())
                    } else {
                        NoSearchResultsPlaceholder(
                            title = stringResource(Res.string.insights_search_empty_title),
                            onClearSearch = { searchState.clearText() },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                },
            ) { insights ->
                LazyColumn(contentPadding = PaddingValues(bottom = Spacing.large) + contentPadding) {
                    itemsIndexed(insights, key = { _, insight -> insight.id }) { index, insight ->
                        InsightRow(
                            insight = insight,
                            divider = index < insights.lastIndex,
                            onClick = { onOpenInsight(insight.id) },
                            onToggleFavorite = { onToggleFavorite(insight) },
                        )
                    }
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(contentPadding),
        )
    }
}

@Composable
private fun InsightRow(insight: InsightSummary, divider: Boolean, onClick: () -> Unit, onToggleFavorite: () -> Unit) {
    EntityRow(
        title = insight.name,
        subtitle = insight.description,
        divider = divider,
        onClick = onClick,
        meta = {
            Tag(text = insight.kind.label())
            insight.lastModifiedAt?.let { MetaText(it.formatRelative()) }
        },
        trailing = {
            // Pull the 48dp touch target outward so the icon itself sits on the 16dp edge like other trailing content.
            FavoriteIconButton(
                favorited = insight.favorited,
                onClick = onToggleFavorite,
                modifier = Modifier.offset(x = 12.dp),
            )
        },
    )
}

/** Star outline in `onSurfaceVariant`, filled `primary` once favourited. */
@Composable
internal fun FavoriteIconButton(favorited: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
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
    onQueryChanged = {},
    onRefresh = {},
    onToggleFavorite = {},
    onOpenInsight = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun InsightsScreenEmptyPreview() = InsightsScreen(
    state = InsightsUiState(query = "retention", insights = CachedUiState.Empty(isRefreshing = false)),
    snackbarHostState = remember { SnackbarHostState() },
    onQueryChanged = {},
    onRefresh = {},
    onToggleFavorite = {},
    onOpenInsight = {},
)
