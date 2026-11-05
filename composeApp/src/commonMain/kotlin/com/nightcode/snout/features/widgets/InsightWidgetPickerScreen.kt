package com.nightcode.snout.features.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.compose.bottomOnly
import com.nightcode.snout.common.compose.withoutBottom
import com.nightcode.snout.common.format.formatRelative
import com.nightcode.snout.designsystem.atoms.SectionLabel
import com.nightcode.snout.designsystem.atoms.Tag
import com.nightcode.snout.designsystem.atoms.textfields.SearchField
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.molecules.MetaText
import com.nightcode.snout.designsystem.molecules.Placeholder
import com.nightcode.snout.designsystem.organisms.LoadingOrErrorContent
import com.nightcode.snout.designsystem.organisms.NoSearchResultsPlaceholder
import com.nightcode.snout.designsystem.organisms.NothingFoundPlaceholder
import com.nightcode.snout.designsystem.organisms.PushedScreenScaffold
import com.nightcode.snout.designsystem.organisms.RefreshableContent
import com.nightcode.snout.designsystem.organisms.RefreshableEmptyContent
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.features.insights.components.label
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.insights.InsightSummary
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.dialog_cancel
import snout.composeapp.generated.resources.ic_insights_24dp
import snout.composeapp.generated.resources.insights_search_empty_title
import snout.composeapp.generated.resources.insights_search_placeholder
import snout.composeapp.generated.resources.widget_insight_picker_footnote
import snout.composeapp.generated.resources.widget_insight_picker_insight_section
import snout.composeapp.generated.resources.widget_insight_picker_not_connected_message
import snout.composeapp.generated.resources.widget_insight_picker_not_connected_title
import snout.composeapp.generated.resources.widget_insight_picker_title
import snout.composeapp.generated.resources.widget_project_choose_first
import snout.composeapp.generated.resources.widget_project_title

@Composable
internal fun InsightWidgetPickerScreen(
    state: InsightWidgetPickerUiState,
    onProjectSelected: (Long) -> Unit,
    onRetryProjects: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onRefresh: () -> Unit,
    onInsightPicked: (InsightSummary) -> Unit,
    onClose: () -> Unit,
) {
    PushedScreenScaffold(
        title = stringResource(Res.string.widget_insight_picker_title),
        onNavigateBack = onClose,
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding.withoutBottom())
        when (state.isConnected) {
            null -> LoadingOrErrorContent(error = null, onRetry = onRefresh, modifier = modifier)

            false -> Placeholder(
                icon = painterResource(Res.drawable.ic_insights_24dp),
                title = stringResource(Res.string.widget_insight_picker_not_connected_title),
                message = stringResource(Res.string.widget_insight_picker_not_connected_message),
                actionLabel = stringResource(Res.string.dialog_cancel),
                onAction = onClose,
                modifier = modifier.fillMaxSize().wrapContentHeight(),
            )

            true -> Column(modifier = modifier.fillMaxSize()) {
                SectionLabel(stringResource(Res.string.widget_project_title))
                val projectsFailed = state.projectsError != null
                WidgetProjectRow(state.projects, projectsFailed, state.projectId, onProjectSelected, onRetryProjects)
                SectionLabel(stringResource(Res.string.widget_insight_picker_insight_section))
                // Insights are listed once their project is known, so a pick always has the project it saves with.
                val project = state.projects?.firstOrNull { it.id == state.projectId }
                when {
                    state.projectId != null && state.projects == null ->
                        LoadingOrErrorContent(error = state.projectsError, onRetry = onRetryProjects)

                    project == null -> Text(
                        text = stringResource(Res.string.widget_project_choose_first),
                        style = SnoutTheme.typography.bodyMedium,
                        color = SnoutTheme.colors.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = Spacing.large),
                    )

                    // Keyed on the project, so another project starts with an empty search.
                    else -> key(state.projectId) {
                        InsightList(state, onQueryChanged, onRefresh, onInsightPicked, innerPadding.bottomOnly())
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightList(
    state: InsightWidgetPickerUiState,
    onQueryChanged: (String) -> Unit,
    onRefresh: () -> Unit,
    onInsightPicked: (InsightSummary) -> Unit,
    contentPadding: PaddingValues,
) {
    val searchState = rememberTextFieldState(state.query)
    LaunchedEffect(searchState.text) { onQueryChanged(searchState.text.toString()) }

    Column(modifier = Modifier.fillMaxSize()) {
        SearchField(
            state = searchState,
            placeholder = stringResource(Res.string.insights_search_placeholder),
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.large, vertical = Spacing.small),
        )
        // A query shows its search results instead of the project's insights.
        val isSearching = state.query.isNotBlank()
        val insights = if (isSearching) state.searchResults else state.insights
        val error = if (isSearching) state.searchError else state.error
        val isRefreshing = !isSearching && state.isRefreshing
        when {
            insights == null -> LoadingOrErrorContent(error = error, onRetry = onRefresh)

            insights.isEmpty() -> RefreshableEmptyContent(isRefreshing = isRefreshing, onRefresh = onRefresh) {
                if (isSearching) {
                    NoSearchResultsPlaceholder(
                        title = stringResource(Res.string.insights_search_empty_title),
                        onClearSearch = { searchState.clearText() },
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    NothingFoundPlaceholder(onRetry = onRefresh, modifier = Modifier.fillMaxSize())
                }
            }

            else -> RefreshableContent(isRefreshing = isRefreshing, refreshError = error, onRefresh = onRefresh) {
                LazyColumn(contentPadding = PaddingValues(bottom = Spacing.large) + contentPadding) {
                    itemsIndexed(insights, key = { _, insight -> insight.id }) { index, insight ->
                        EntityRow(
                            title = insight.name,
                            subtitle = insight.description,
                            divider = index < insights.lastIndex,
                            onClick = { onInsightPicked(insight) },
                            meta = {
                                Tag(text = stringResource(insight.kind.label))
                                insight.lastModifiedAt?.let { MetaText(it.formatRelative()) }
                            },
                        )
                    }
                    item(key = "footnote") {
                        Text(
                            text = stringResource(Res.string.widget_insight_picker_footnote),
                            style = SnoutTheme.typography.bodySmall,
                            color = SnoutTheme.colors.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = Spacing.large, vertical = Spacing.medium),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun InsightWidgetPickerScreenPreview() = InsightWidgetPickerScreen(
    state = TestInsightWidgetPickerUiState,
    onProjectSelected = {},
    onRetryProjects = {},
    onQueryChanged = {},
    onRefresh = {},
    onInsightPicked = {},
    onClose = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun InsightWidgetPickerScreenNotConnectedPreview() = InsightWidgetPickerScreen(
    state = InsightWidgetPickerUiState(isConnected = false),
    onProjectSelected = {},
    onRetryProjects = {},
    onQueryChanged = {},
    onRefresh = {},
    onInsightPicked = {},
    onClose = {},
)
