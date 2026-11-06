package com.nightcode.snout.features.persons.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.compose.bottomOnly
import com.nightcode.snout.common.compose.withoutBottom
import com.nightcode.snout.common.format.formatDateTime
import com.nightcode.snout.designsystem.atoms.AvatarBox
import com.nightcode.snout.designsystem.atoms.SkeletonRows
import com.nightcode.snout.designsystem.molecules.KeyValueRow
import com.nightcode.snout.designsystem.molecules.MetaText
import com.nightcode.snout.designsystem.molecules.Placeholder
import com.nightcode.snout.designsystem.molecules.SegmentedControl
import com.nightcode.snout.designsystem.organisms.DetailLoadingRowCount
import com.nightcode.snout.designsystem.organisms.ErrorPlaceholder
import com.nightcode.snout.designsystem.organisms.LoadMoreFooter
import com.nightcode.snout.designsystem.organisms.LoadingOrErrorContent
import com.nightcode.snout.designsystem.organisms.PushedScreenScaffold
import com.nightcode.snout.designsystem.organisms.RefreshableContent
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.features.persons.components.PersonEventRow
import com.nightcode.snout.features.persons.components.PersonExceptionRow
import com.nightcode.snout.features.persons.components.PersonFlagRow
import com.nightcode.snout.features.persons.components.label
import com.nightcode.snout.features.persons.components.secondaryLabel
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.persons.Person
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_bug_report_24dp
import snout.composeapp.generated.resources.ic_flag_24dp
import snout.composeapp.generated.resources.ic_history_24dp
import snout.composeapp.generated.resources.person_created
import snout.composeapp.generated.resources.person_distinct_ids
import snout.composeapp.generated.resources.person_errors_empty_subtitle
import snout.composeapp.generated.resources.person_errors_empty_title
import snout.composeapp.generated.resources.person_events_empty_subtitle
import snout.composeapp.generated.resources.person_events_empty_title
import snout.composeapp.generated.resources.person_events_load_more_failed
import snout.composeapp.generated.resources.person_flags_empty_subtitle
import snout.composeapp.generated.resources.person_flags_empty_title
import snout.composeapp.generated.resources.person_properties_empty
import snout.composeapp.generated.resources.person_title
import snout.composeapp.generated.resources.placeholder_nothing_found_retry

private const val PropertyValueLines = 4
private const val DistinctIdLines = 3
private const val TabLoadingRows = 3

@Composable
internal fun PersonDetailScreen(
    state: PersonDetailUiState,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onTabSelected: (PersonTab) -> Unit,
    onEventsEndReached: () -> Unit,
    onOpenIssue: (issueId: String) -> Unit,
    onOpenInPostHog: () -> Unit,
) {
    val person = state.person
    PushedScreenScaffold(
        title = stringResource(Res.string.person_title),
        onNavigateBack = onNavigateBack,
        onOpenInPostHog = onOpenInPostHog,
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding.withoutBottom())
        if (person == null) {
            LoadingOrErrorContent(
                error = state.error,
                onRetry = onRefresh,
                modifier = modifier,
                loadingContent = { SkeletonRows(count = DetailLoadingRowCount) },
            )
        } else {
            RefreshableContent(
                isRefreshing = state.isRefreshing,
                refreshError = state.error,
                onRefresh = onRefresh,
                modifier = modifier,
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = Spacing.xxLarge) + innerPadding.bottomOnly(),
                ) {
                    item(key = "header", contentType = "header") { PersonHeader(person) }
                    item(key = "tabs", contentType = "tabs") {
                        SegmentedControl(
                            options = PersonTab.entries,
                            selected = state.tab,
                            onSelect = onTabSelected,
                            label = { stringResource(it.label) },
                            modifier = Modifier.padding(horizontal = Spacing.large, vertical = Spacing.medium),
                        )
                    }
                    when (state.tab) {
                        PersonTab.Properties -> propertyItems(person.properties)
                        PersonTab.Events -> eventItems(state, onRefresh, onEventsEndReached)
                        PersonTab.Flags -> flagItems(state, onRefresh)
                        PersonTab.Errors -> exceptionItems(state, onRefresh, onOpenIssue)
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonHeader(person: Person) {
    Column {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.large, vertical = Spacing.small),
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarBox(name = person.displayName, size = 48.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = person.displayName,
                    style = SnoutTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                person.secondaryLabel?.let { MetaText(it) }
            }
        }
        if (person.distinctIds.isNotEmpty()) {
            KeyValueRow(
                label = stringResource(Res.string.person_distinct_ids),
                value = person.distinctIds.joinToString("\n"),
                valueIsCode = true,
                valueMaxLines = DistinctIdLines,
            )
        }
        person.createdAt?.let {
            KeyValueRow(label = stringResource(Res.string.person_created), value = it.formatDateTime())
        }
    }
}

private fun LazyListScope.propertyItems(properties: Map<String, String>) {
    if (properties.isEmpty()) {
        item(key = "properties-empty") {
            MetaText(
                text = stringResource(Res.string.person_properties_empty),
                modifier = Modifier.padding(horizontal = Spacing.large, vertical = Spacing.small),
            )
        }
        return
    }
    items(properties.entries.toList(), key = { "property-${it.key}" }, contentType = { "property" }) { (key, value) ->
        KeyValueRow(label = key, value = value, valueIsCode = true, valueMaxLines = PropertyValueLines)
    }
}

private fun LazyListScope.eventItems(state: PersonDetailUiState, onRetry: () -> Unit, onEventsEndReached: () -> Unit) {
    val events = state.events
    when {
        events == null -> tabLoadingOrError(state.eventsError, onRetry)

        events.isEmpty() -> tabEmpty(
            icon = { painterResource(Res.drawable.ic_history_24dp) },
            title = { stringResource(Res.string.person_events_empty_title) },
            message = { stringResource(Res.string.person_events_empty_subtitle) },
            onRefresh = onRetry,
        )

        else -> {
            itemsIndexed(events, key = { _, event -> "event-${event.id}" }, contentType = { _, _ -> "event" }) { i, e ->
                PersonEventRow(event = e, divider = i < events.lastIndex)
            }
            item(key = "events-load-more", contentType = "load-more") {
                LoadMoreFooter(
                    status = state.eventsLoadMore,
                    failedText = stringResource(Res.string.person_events_load_more_failed),
                    onLoadMore = onEventsEndReached,
                )
            }
        }
    }
}

private fun LazyListScope.flagItems(state: PersonDetailUiState, onRetry: () -> Unit) {
    val flags = state.flags
    when {
        flags == null -> tabLoadingOrError(state.flagsError, onRetry)

        flags.isEmpty() -> tabEmpty(
            icon = { painterResource(Res.drawable.ic_flag_24dp) },
            title = { stringResource(Res.string.person_flags_empty_title) },
            message = { stringResource(Res.string.person_flags_empty_subtitle) },
            onRefresh = onRetry,
        )

        else -> itemsIndexed(flags, key = { _, flag -> "flag-${flag.key}" }, contentType = { _, _ -> "flag" }) { i, f ->
            PersonFlagRow(flag = f, divider = i < flags.lastIndex)
        }
    }
}

private fun LazyListScope.exceptionItems(
    state: PersonDetailUiState,
    onRetry: () -> Unit,
    onOpenIssue: (issueId: String) -> Unit,
) {
    val exceptions = state.exceptions
    when {
        exceptions == null -> tabLoadingOrError(state.exceptionsError, onRetry)

        exceptions.isEmpty() -> tabEmpty(
            icon = { painterResource(Res.drawable.ic_bug_report_24dp) },
            title = { stringResource(Res.string.person_errors_empty_title) },
            message = { stringResource(Res.string.person_errors_empty_subtitle) },
            onRefresh = onRetry,
        )

        else -> itemsIndexed(
            exceptions,
            key = { _, exception -> "exception-${exception.id}" },
            contentType = { _, _ -> "exception" },
        ) { index, exception ->
            PersonExceptionRow(
                exception = exception,
                divider = index < exceptions.lastIndex,
                onClick = exception.issueId?.let { issueId -> { onOpenIssue(issueId) } },
            )
        }
    }
}

private fun LazyListScope.tabLoadingOrError(error: DataError?, onRetry: () -> Unit) {
    item(key = "tab-state", contentType = "tab-state") {
        if (error == null) {
            SkeletonRows(count = TabLoadingRows)
        } else {
            ErrorPlaceholder(error = error, onRetry = onRetry, modifier = Modifier.padding(vertical = Spacing.xLarge))
        }
    }
}

private fun LazyListScope.tabEmpty(
    icon: @Composable () -> Painter,
    title: @Composable () -> String,
    message: @Composable () -> String,
    onRefresh: () -> Unit,
) {
    item(key = "tab-empty", contentType = "tab-state") {
        Placeholder(
            icon = icon(),
            title = title(),
            message = message(),
            actionLabel = stringResource(Res.string.placeholder_nothing_found_retry),
            onAction = onRefresh,
            modifier = Modifier.padding(vertical = Spacing.xLarge),
        )
    }
}

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun PersonDetailScreenPreview() = PersonDetailScreen(
    state = TestPersonDetailUiState,
    onNavigateBack = {},
    onRefresh = {},
    onTabSelected = {},
    onEventsEndReached = {},
    onOpenIssue = {},
    onOpenInPostHog = {},
)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun PersonDetailScreenEventsPreview() = PersonDetailScreen(
    state = TestPersonDetailUiState.copy(tab = PersonTab.Events),
    onNavigateBack = {},
    onRefresh = {},
    onTabSelected = {},
    onEventsEndReached = {},
    onOpenIssue = {},
    onOpenInPostHog = {},
)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun PersonDetailScreenFlagsPreview() = PersonDetailScreen(
    state = TestPersonDetailUiState.copy(tab = PersonTab.Flags),
    onNavigateBack = {},
    onRefresh = {},
    onTabSelected = {},
    onEventsEndReached = {},
    onOpenIssue = {},
    onOpenInPostHog = {},
)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun PersonDetailScreenErrorsPreview() = PersonDetailScreen(
    state = TestPersonDetailUiState.copy(tab = PersonTab.Errors),
    onNavigateBack = {},
    onRefresh = {},
    onTabSelected = {},
    onEventsEndReached = {},
    onOpenIssue = {},
    onOpenInPostHog = {},
)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun PersonDetailScreenFlagsForbiddenPreview() = PersonDetailScreen(
    state = TestPersonDetailUiState.copy(tab = PersonTab.Flags, flags = null, flagsError = DataError.Forbidden),
    onNavigateBack = {},
    onRefresh = {},
    onTabSelected = {},
    onEventsEndReached = {},
    onOpenIssue = {},
    onOpenInPostHog = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun PersonDetailScreenNotFoundPreview() = PersonDetailScreen(
    state = PersonDetailUiState(error = DataError.NotFound),
    onNavigateBack = {},
    onRefresh = {},
    onTabSelected = {},
    onEventsEndReached = {},
    onOpenIssue = {},
    onOpenInPostHog = {},
)
