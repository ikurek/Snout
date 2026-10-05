package com.nightcode.snout.features.insights.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.compose.rememberOpenInPostHog
import com.nightcode.snout.common.format.formatDateTime
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.designsystem.atoms.LabelChip
import com.nightcode.snout.designsystem.atoms.buttons.NavigationBackIconButton
import com.nightcode.snout.designsystem.atoms.buttons.OpenInPostHogIconButton
import com.nightcode.snout.designsystem.molecules.KeyValueRow
import com.nightcode.snout.designsystem.molecules.SectionCard
import com.nightcode.snout.designsystem.organisms.ErrorPlaceholder
import com.nightcode.snout.designsystem.organisms.LoadingIndicator
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.features.insights.FavoriteIconButton
import com.nightcode.snout.features.insights.components.InsightResultView
import com.nightcode.snout.features.insights.components.label
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.appstate.PostHogResource
import com.nightcode.snout.shared.domain.model.insights.InsightDetail
import com.nightcode.snout.shared.domain.model.insights.TestInsightDetail
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.insight_detail_kind
import snout.composeapp.generated.resources.insight_detail_last_modified
import snout.composeapp.generated.resources.insight_detail_tags
import snout.composeapp.generated.resources.insight_result_pending
import snout.composeapp.generated.resources.insight_title_placeholder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InsightDetailScreen(
    state: CachedUiState<InsightDetail>,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit,
    onFavoriteToggle: () -> Unit,
) {
    val detail = (state as? CachedUiState.Content)?.data
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = detail?.summary?.name ?: stringResource(Res.string.insight_title_placeholder),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = { NavigationBackIconButton(onClick = onNavigateBack) },
                actions = {
                    if (detail != null) {
                        FavoriteIconButton(favorited = detail.summary.favorited, onClick = onFavoriteToggle)
                        OpenInPostHogIconButton(
                            onClick = rememberOpenInPostHog(PostHogResource.Insight(detail.summary.shortId)),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        val modifier = Modifier.fillMaxSize().padding(innerPadding)
        when (state) {
            CachedUiState.Loading, is CachedUiState.Empty -> LoadingIndicator(modifier)
            is CachedUiState.Error -> ErrorPlaceholder(error = state.error, onRetry = onRetry, modifier = modifier)
            is CachedUiState.Content -> DetailContent(state.data, modifier)
        }
    }
}

@Composable
private fun DetailContent(detail: InsightDetail, modifier: Modifier) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionCard(title = detail.summary.name, subtitle = detail.summary.description) {
            val result = detail.result
            if (result == null) {
                Text(
                    text = stringResource(Res.string.insight_result_pending),
                    style = SnoutTheme.typography.bodySmall,
                    color = SnoutTheme.colors.onSurfaceVariant,
                )
            } else {
                InsightResultView(result = result, display = detail.summary.display, chartHeight = 240.dp)
            }
        }
        KeyValueRow(label = stringResource(Res.string.insight_detail_kind), value = detail.summary.kind.label())
        detail.summary.lastModifiedAt?.let {
            KeyValueRow(label = stringResource(Res.string.insight_detail_last_modified), value = it.formatDateTime())
        }
        if (detail.summary.tags.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(Res.string.insight_detail_tags),
                    style = SnoutTheme.typography.labelMedium,
                    color = SnoutTheme.colors.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    detail.summary.tags.forEach { LabelChip(text = it) }
                }
            }
        }
    }
}

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
private fun InsightDetailScreenPreview() = InsightDetailScreen(
    state = CachedUiState.Content(TestInsightDetail, isRefreshing = false),
    snackbarHostState = remember { SnackbarHostState() },
    onNavigateBack = {},
    onRetry = {},
    onFavoriteToggle = {},
)
