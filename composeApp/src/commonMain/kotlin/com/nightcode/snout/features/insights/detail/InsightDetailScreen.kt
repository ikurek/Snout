package com.nightcode.snout.features.insights.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.compose.bottomOnly
import com.nightcode.snout.common.compose.withoutBottom
import com.nightcode.snout.common.format.formatDateTime
import com.nightcode.snout.common.state.LoadState
import com.nightcode.snout.common.state.dataOrNull
import com.nightcode.snout.designsystem.atoms.SectionLabel
import com.nightcode.snout.designsystem.atoms.SkeletonRows
import com.nightcode.snout.designsystem.molecules.KeyValueRow
import com.nightcode.snout.designsystem.molecules.SectionCard
import com.nightcode.snout.designsystem.molecules.TagList
import com.nightcode.snout.designsystem.organisms.LoadStateContainer
import com.nightcode.snout.designsystem.organisms.PushedScreenScaffold
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.features.insights.FavoriteIconButton
import com.nightcode.snout.features.insights.components.InsightResultView
import com.nightcode.snout.features.insights.components.label
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.insights.InsightDateRange
import com.nightcode.snout.shared.domain.model.insights.InsightDetail
import com.nightcode.snout.shared.domain.model.insights.InsightDisplay
import com.nightcode.snout.shared.domain.model.insights.InsightKind
import com.nightcode.snout.shared.domain.model.insights.InsightPeriod
import com.nightcode.snout.shared.domain.model.insights.PeriodUnit
import com.nightcode.snout.shared.domain.model.insights.TestComparisonResult
import com.nightcode.snout.shared.domain.model.insights.TestFunnelResult
import com.nightcode.snout.shared.domain.model.insights.TestInsightDetail
import com.nightcode.snout.shared.domain.model.insights.TestInsightSummary
import com.nightcode.snout.shared.domain.model.insights.TestRetentionResult
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.insight_detail_kind
import snout.composeapp.generated.resources.insight_detail_last_modified
import snout.composeapp.generated.resources.insight_detail_section
import snout.composeapp.generated.resources.insight_detail_tags
import snout.composeapp.generated.resources.insight_result_pending
import snout.composeapp.generated.resources.insight_title_placeholder

private const val LoadingRowCount = 3

@Composable
internal fun InsightDetailScreen(
    state: InsightDetailUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenInPostHog: () -> Unit,
) {
    val detail = state.dataOrNull
    PushedScreenScaffold(
        title = detail?.summary?.name ?: stringResource(Res.string.insight_title_placeholder),
        onNavigateBack = onNavigateBack,
        // Both actions need the loaded insight.
        onOpenInPostHog = onOpenInPostHog.takeIf { detail != null },
        actions = {
            if (detail != null) {
                FavoriteIconButton(favorited = detail.summary.favorited, onClick = onToggleFavorite)
            }
        },
        snackbarHostState = snackbarHostState,
    ) { innerPadding ->
        LoadStateContainer(
            state = state,
            onRefresh = onRefresh,
            modifier = Modifier.padding(innerPadding.withoutBottom()),
            loadingContent = { SkeletonRows(count = LoadingRowCount) },
        ) { detail ->
            DetailContent(detail, Modifier.fillMaxSize(), innerPadding.bottomOnly())
        }
    }
}

@Composable
private fun DetailContent(detail: InsightDetail, modifier: Modifier, contentPadding: PaddingValues) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(top = Spacing.small, bottom = Spacing.xxLarge),
    ) {
        SectionCard(
            title = detail.summary.name,
            subtitle = detail.summary.description,
            modifier = Modifier.padding(horizontal = Spacing.large),
        ) {
            detail.summary.period?.let {
                Text(
                    text = it.label(),
                    style = SnoutTheme.typography.bodySmall,
                    color = SnoutTheme.colors.onSurfaceVariant,
                )
            }
            val result = detail.result
            if (result == null) {
                Text(
                    text = stringResource(Res.string.insight_result_pending),
                    style = SnoutTheme.typography.bodyMedium,
                    color = SnoutTheme.colors.onSurfaceVariant,
                )
            } else {
                InsightResultView(
                    result = result,
                    display = detail.summary.display,
                    chartHeight = 240.dp,
                    detailed = true,
                )
            }
        }
        SectionLabel(stringResource(Res.string.insight_detail_section))
        KeyValueRow(label = stringResource(Res.string.insight_detail_kind), value = detail.summary.kind.label())
        detail.summary.lastModifiedAt?.let {
            KeyValueRow(label = stringResource(Res.string.insight_detail_last_modified), value = it.formatDateTime())
        }
        if (detail.summary.tags.isNotEmpty()) {
            TagList(label = stringResource(Res.string.insight_detail_tags), tags = detail.summary.tags)
        }
    }
}

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun InsightDetailScreenPreview() = InsightDetailScreen(
    state = LoadState.Loaded(TestInsightDetail),
    snackbarHostState = remember { SnackbarHostState() },
    onNavigateBack = {},
    onRefresh = {},
    onToggleFavorite = {},
    onOpenInPostHog = {},
)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun InsightDetailScreenRetentionPreview() = InsightDetailScreen(
    state = LoadState.Loaded(
        TestInsightDetail.copy(
            summary = TestInsightSummary.copy(
                name = "Day 1 retention",
                kind = InsightKind.Retention,
                period = InsightPeriod(InsightDateRange.Last(4, PeriodUnit.Day)),
            ),
            result = TestRetentionResult,
        ),
    ),
    snackbarHostState = remember { SnackbarHostState() },
    onNavigateBack = {},
    onRefresh = {},
    onToggleFavorite = {},
    onOpenInPostHog = {},
)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun InsightDetailScreenComparisonPreview() = InsightDetailScreen(
    state = LoadState.Loaded(
        TestInsightDetail.copy(
            summary = TestInsightSummary.copy(
                name = "Pageviews by browser",
                description = null,
                period = InsightPeriod(InsightDateRange.Last(5, PeriodUnit.Day), PeriodUnit.Day, compared = true),
            ),
            result = TestComparisonResult,
        ),
    ),
    snackbarHostState = remember { SnackbarHostState() },
    onNavigateBack = {},
    onRefresh = {},
    onToggleFavorite = {},
    onOpenInPostHog = {},
)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun InsightDetailScreenFunnelPreview() = InsightDetailScreen(
    state = LoadState.Loaded(
        TestInsightDetail.copy(
            summary = TestInsightSummary.copy(
                name = "Checkout by browser",
                description = null,
                kind = InsightKind.Funnels,
                display = InsightDisplay.Funnel,
                period = InsightPeriod(InsightDateRange.Last(30, PeriodUnit.Day)),
            ),
            result = TestFunnelResult,
        ),
    ),
    snackbarHostState = remember { SnackbarHostState() },
    onNavigateBack = {},
    onRefresh = {},
    onToggleFavorite = {},
    onOpenInPostHog = {},
)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun InsightDetailScreenPendingPreview() = InsightDetailScreen(
    state = LoadState.Loaded(TestInsightDetail.copy(result = null)),
    snackbarHostState = remember { SnackbarHostState() },
    onNavigateBack = {},
    onRefresh = {},
    onToggleFavorite = {},
    onOpenInPostHog = {},
)
