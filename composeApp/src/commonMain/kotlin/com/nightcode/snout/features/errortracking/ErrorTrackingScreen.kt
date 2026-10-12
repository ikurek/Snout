package com.nightcode.snout.features.errortracking

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.format.formatCompact
import com.nightcode.snout.common.format.formatRelative
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.designsystem.atoms.textfields.SearchField
import com.nightcode.snout.designsystem.charts.Sparkline
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.molecules.FilterMenuChip
import com.nightcode.snout.designsystem.molecules.MenuOption
import com.nightcode.snout.designsystem.molecules.MetaText
import com.nightcode.snout.designsystem.molecules.Placeholder
import com.nightcode.snout.designsystem.organisms.CachedStateContainer
import com.nightcode.snout.designsystem.organisms.NoSearchResultsPlaceholder
import com.nightcode.snout.designsystem.organisms.UnavailablePlaceholder
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.features.errortracking.components.IssueSeverityTag
import com.nightcode.snout.features.errortracking.components.label
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.errortracking.AppVersion
import com.nightcode.snout.shared.domain.model.errortracking.ErrorTrackingIssue
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter
import com.nightcode.snout.shared.domain.model.errortracking.TestAppVersions
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.error_tracking_empty_subtitle
import snout.composeapp.generated.resources.error_tracking_empty_title
import snout.composeapp.generated.resources.error_tracking_occurrences_count
import snout.composeapp.generated.resources.error_tracking_search_empty_title
import snout.composeapp.generated.resources.error_tracking_search_placeholder
import snout.composeapp.generated.resources.error_tracking_unavailable_subtitle
import snout.composeapp.generated.resources.error_tracking_unavailable_title
import snout.composeapp.generated.resources.error_tracking_users_count
import snout.composeapp.generated.resources.error_tracking_version_all
import snout.composeapp.generated.resources.error_tracking_version_empty_subtitle
import snout.composeapp.generated.resources.error_tracking_version_empty_title
import snout.composeapp.generated.resources.error_tracking_version_exceptions
import snout.composeapp.generated.resources.error_tracking_version_selected
import snout.composeapp.generated.resources.ic_check_24dp
import snout.composeapp.generated.resources.ic_search_24dp
import snout.composeapp.generated.resources.placeholder_nothing_found_retry

@Composable
internal fun ErrorTrackingScreen(
    state: ErrorTrackingUiState,
    onQueryChanged: (String) -> Unit,
    onStatusChanged: (IssueStatusFilter) -> Unit,
    onAppVersionChanged: (String?) -> Unit,
    onRefresh: () -> Unit,
    onOpenIssue: (issueId: String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val searchState = rememberTextFieldState(state.query)
    LaunchedEffect(searchState.text) { onQueryChanged(searchState.text.toString()) }

    Column(modifier = Modifier.fillMaxSize()) {
        SearchField(
            state = searchState,
            placeholder = stringResource(Res.string.error_tracking_search_placeholder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.large)
                .padding(top = Spacing.small, bottom = Spacing.xxSmall),
        )
        IssueFilters(
            status = state.status,
            onStatusChanged = onStatusChanged,
            appVersion = state.appVersion,
            appVersions = state.appVersions,
            onAppVersionChanged = onAppVersionChanged,
        )
        CachedStateContainer(
            state = state.issues,
            onRefresh = onRefresh,
            notFoundContent = {
                UnavailablePlaceholder(
                    title = stringResource(Res.string.error_tracking_unavailable_title),
                    message = stringResource(Res.string.error_tracking_unavailable_subtitle),
                    onRetry = onRefresh,
                    modifier = Modifier.fillMaxSize(),
                )
            },
            emptyContent = {
                IssuesEmptyContent(
                    query = state.query,
                    appVersion = state.appVersion,
                    onClearSearch = { searchState.clearText() },
                    onClearAppVersion = { onAppVersionChanged(null) },
                    onRefresh = onRefresh,
                )
            },
        ) { list ->
            LazyColumn(contentPadding = PaddingValues(bottom = Spacing.large) + contentPadding) {
                itemsIndexed(list, key = { _, issue -> issue.id }) { index, issue ->
                    IssueRow(issue = issue, divider = index < list.lastIndex, onClick = { onOpenIssue(issue.id) })
                }
            }
        }
    }
}

/** Empty list: a search with no match, a version with no issues, or simply nothing with this status. */
@Composable
private fun IssuesEmptyContent(
    query: String,
    appVersion: String?,
    onClearSearch: () -> Unit,
    onClearAppVersion: () -> Unit,
    onRefresh: () -> Unit,
) {
    val modifier = Modifier.fillMaxSize()
    when {
        query.isNotBlank() -> NoSearchResultsPlaceholder(
            title = stringResource(Res.string.error_tracking_search_empty_title),
            onClearSearch = onClearSearch,
            modifier = modifier,
        )

        appVersion != null -> Placeholder(
            icon = painterResource(Res.drawable.ic_search_24dp),
            title = stringResource(Res.string.error_tracking_version_empty_title, appVersion),
            message = stringResource(Res.string.error_tracking_version_empty_subtitle),
            actionLabel = stringResource(Res.string.error_tracking_version_all),
            onAction = onClearAppVersion,
            modifier = modifier,
        )

        else -> Placeholder(
            icon = painterResource(Res.drawable.ic_check_24dp),
            title = stringResource(Res.string.error_tracking_empty_title),
            message = stringResource(Res.string.error_tracking_empty_subtitle),
            actionLabel = stringResource(Res.string.placeholder_nothing_found_retry),
            onAction = onRefresh,
            modifier = modifier,
        )
    }
}

/**
 * One scrollable row of dropdown filter chips under the search field. The version chip only appears when PostHog
 * reported app versions, which web projects don't send.
 */
@Composable
private fun IssueFilters(
    status: IssueStatusFilter,
    onStatusChanged: (IssueStatusFilter) -> Unit,
    appVersion: String?,
    appVersions: List<AppVersion>,
    onAppVersionChanged: (String?) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Spacing.large, vertical = Spacing.xxSmall),
    ) {
        FilterMenuChip(
            label = stringResource(status.label),
            options = IssueStatusFilter.entries.map { MenuOption(it, stringResource(it.label)) },
            selected = status,
            onSelect = onStatusChanged,
        )
        if (appVersions.isNotEmpty() || appVersion != null) {
            val allVersions = stringResource(Res.string.error_tracking_version_all)
            FilterMenuChip(
                label = appVersion?.let { stringResource(Res.string.error_tracking_version_selected, it) }
                    ?: allVersions,
                options = listOf(MenuOption<String?>(null, allVersions)) + appVersions.map { version ->
                    MenuOption<String?>(
                        value = version.name,
                        label = version.name,
                        supportingText = pluralStringResource(
                            Res.plurals.error_tracking_version_exceptions,
                            version.exceptions.toPluralQuantity(),
                            version.exceptions.toDouble().formatCompact(),
                        ),
                    )
                },
                selected = appVersion,
                onSelect = onAppVersionChanged,
                active = appVersion != null,
            )
        }
    }
}

/** Exception type, message, then severity and the week's impact; the sparkline shows whether it is growing. */
@Composable
private fun IssueRow(issue: ErrorTrackingIssue, divider: Boolean, onClick: () -> Unit) {
    EntityRow(
        title = issue.name,
        titleMaxLines = 1,
        subtitle = issue.description,
        divider = divider,
        onClick = onClick,
        meta = {
            issue.severity?.let { IssueSeverityTag(it) }
            MetaText(issue.impactSummary(), modifier = Modifier.weight(1f, fill = false))
        },
        trailing = { Sparkline(values = issue.volume.map { it.toDouble() }) },
    )
}

/** "1.3k occurrences · 312 users · 2 h ago" */
@Composable
private fun ErrorTrackingIssue.impactSummary(): String = listOfNotNull(
    pluralStringResource(
        Res.plurals.error_tracking_occurrences_count,
        occurrences.toPluralQuantity(),
        occurrences.toDouble().formatCompact(),
    ),
    pluralStringResource(
        Res.plurals.error_tracking_users_count,
        users.toPluralQuantity(),
        users.toDouble().formatCompact(),
    ),
    lastSeen?.formatRelative(),
).joinToString(" · ")

/** Plural rules only need to tell one from many; counts beyond Int range are "many" either way. */
private fun Long.toPluralQuantity(): Int = coerceAtMost(Int.MAX_VALUE.toLong()).toInt()

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ErrorTrackingScreenPreview() = ErrorTrackingScreen(
    state = TestErrorTrackingUiState,
    onQueryChanged = {},
    onStatusChanged = {},
    onAppVersionChanged = {},
    onRefresh = {},
    onOpenIssue = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ErrorTrackingScreenEmptyPreview() = ErrorTrackingScreen(
    state = ErrorTrackingUiState(issues = CachedUiState.Empty(isRefreshing = false)),
    onQueryChanged = {},
    onStatusChanged = {},
    onAppVersionChanged = {},
    onRefresh = {},
    onOpenIssue = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ErrorTrackingScreenUnavailablePreview() = ErrorTrackingScreen(
    state = ErrorTrackingUiState(issues = CachedUiState.Error(DataError.NotFound)),
    onQueryChanged = {},
    onStatusChanged = {},
    onAppVersionChanged = {},
    onRefresh = {},
    onOpenIssue = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ErrorTrackingScreenForbiddenPreview() = ErrorTrackingScreen(
    state = ErrorTrackingUiState(issues = CachedUiState.Error(DataError.Forbidden)),
    onQueryChanged = {},
    onStatusChanged = {},
    onAppVersionChanged = {},
    onRefresh = {},
    onOpenIssue = {},
)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ErrorTrackingScreenVersionEmptyPreview() = ErrorTrackingScreen(
    state = ErrorTrackingUiState(
        appVersion = "2.13.2",
        appVersions = TestAppVersions,
        issues = CachedUiState.Empty(isRefreshing = false),
    ),
    onQueryChanged = {},
    onStatusChanged = {},
    onAppVersionChanged = {},
    onRefresh = {},
    onOpenIssue = {},
)
