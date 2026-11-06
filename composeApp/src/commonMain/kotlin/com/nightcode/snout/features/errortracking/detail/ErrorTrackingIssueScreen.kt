package com.nightcode.snout.features.errortracking.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.compose.bottomOnly
import com.nightcode.snout.common.compose.withoutBottom
import com.nightcode.snout.common.format.formatDateTime
import com.nightcode.snout.common.format.formatRelative
import com.nightcode.snout.common.format.lastDaysLabels
import com.nightcode.snout.designsystem.atoms.SectionLabel
import com.nightcode.snout.designsystem.atoms.SkeletonRows
import com.nightcode.snout.designsystem.atoms.buttons.PrimaryButton
import com.nightcode.snout.designsystem.atoms.buttons.SecondaryButton
import com.nightcode.snout.designsystem.charts.BarChart
import com.nightcode.snout.designsystem.charts.ChartSeries
import com.nightcode.snout.designsystem.charts.chartColor
import com.nightcode.snout.designsystem.molecules.KeyValueRow
import com.nightcode.snout.designsystem.molecules.StatCard
import com.nightcode.snout.designsystem.organisms.DetailLoadingRowCount
import com.nightcode.snout.designsystem.organisms.LoadingOrErrorContent
import com.nightcode.snout.designsystem.organisms.OptionPickerSheet
import com.nightcode.snout.designsystem.organisms.PickerOption
import com.nightcode.snout.designsystem.organisms.PushedScreenScaffold
import com.nightcode.snout.designsystem.organisms.RefreshableContent
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.features.errortracking.components.IssueSeverityTag
import com.nightcode.snout.features.errortracking.components.IssueStatusPill
import com.nightcode.snout.features.errortracking.components.OccurrenceSection
import com.nightcode.snout.features.errortracking.components.RecentOccurrencesSection
import com.nightcode.snout.shared.common.format.formatCompact
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.errortracking.ErrorTrackingIssue
import com.nightcode.snout.shared.domain.model.errortracking.IssueAssignee
import com.nightcode.snout.shared.domain.model.errortracking.IssueRelease
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatus
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter
import com.nightcode.snout.shared.domain.model.errortracking.StackFrame
import com.nightcode.snout.shared.domain.model.errortracking.TestResolvedErrorTrackingIssue
import com.nightcode.snout.shared.domain.model.organization.OrganizationMember
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.error_tracking_action_reopen
import snout.composeapp.generated.resources.error_tracking_action_resolve
import snout.composeapp.generated.resources.error_tracking_action_suppress
import snout.composeapp.generated.resources.error_tracking_assignee
import snout.composeapp.generated.resources.error_tracking_assignee_member
import snout.composeapp.generated.resources.error_tracking_assignee_picker_title
import snout.composeapp.generated.resources.error_tracking_assignee_role
import snout.composeapp.generated.resources.error_tracking_file
import snout.composeapp.generated.resources.error_tracking_first_seen
import snout.composeapp.generated.resources.error_tracking_function
import snout.composeapp.generated.resources.error_tracking_impact_section
import snout.composeapp.generated.resources.error_tracking_issue_title_placeholder
import snout.composeapp.generated.resources.error_tracking_last_seen
import snout.composeapp.generated.resources.error_tracking_library
import snout.composeapp.generated.resources.error_tracking_location_section
import snout.composeapp.generated.resources.error_tracking_occurrences
import snout.composeapp.generated.resources.error_tracking_overview_section
import snout.composeapp.generated.resources.error_tracking_release_branch
import snout.composeapp.generated.resources.error_tracking_release_commit
import snout.composeapp.generated.resources.error_tracking_release_section
import snout.composeapp.generated.resources.error_tracking_release_version
import snout.composeapp.generated.resources.error_tracking_sessions
import snout.composeapp.generated.resources.error_tracking_severity_label
import snout.composeapp.generated.resources.error_tracking_status_label
import snout.composeapp.generated.resources.error_tracking_unassigned
import snout.composeapp.generated.resources.error_tracking_users
import snout.composeapp.generated.resources.ic_check_24dp

@Composable
internal fun ErrorTrackingIssueScreen(
    state: ErrorTrackingIssueUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onChangeStatus: (IssueStatusFilter) -> Unit,
    onOpenInPostHog: () -> Unit,
    onOccurrenceSelected: (String) -> Unit,
    onExceptionsRetried: () -> Unit,
    onAssigneeClicked: () -> Unit,
    onAssigneeSelected: (Long?) -> Unit,
    onAssigneePickerDismissed: () -> Unit,
) {
    val issue = state.issue
    PushedScreenScaffold(
        title = issue?.name ?: stringResource(Res.string.error_tracking_issue_title_placeholder),
        onNavigateBack = onNavigateBack,
        onOpenInPostHog = onOpenInPostHog,
        snackbarHostState = snackbarHostState,
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding.withoutBottom())
        if (issue == null) {
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
                isPullToRefreshEnabled = false,
            ) {
                IssueContent(
                    issue = issue,
                    state = state,
                    onChangeStatus = onChangeStatus,
                    onRefresh = onRefresh,
                    onOccurrenceSelected = onOccurrenceSelected,
                    onExceptionsRetried = onExceptionsRetried,
                    onAssigneeClicked = onAssigneeClicked,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = innerPadding.bottomOnly(),
                )
            }
        }
    }
    AssigneePicker(
        isVisible = state.isAssigneePickerVisible,
        members = state.members.orEmpty(),
        assignee = issue?.assignee,
        onSelect = onAssigneeSelected,
        onDismiss = onAssigneePickerDismissed,
    )
}

@Suppress("LongParameterList")
@Composable
private fun IssueContent(
    issue: ErrorTrackingIssue,
    state: ErrorTrackingIssueUiState,
    onChangeStatus: (IssueStatusFilter) -> Unit,
    onRefresh: () -> Unit,
    onOccurrenceSelected: (String) -> Unit,
    onExceptionsRetried: () -> Unit,
    onAssigneeClicked: () -> Unit,
    modifier: Modifier,
    contentPadding: PaddingValues,
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var occurrenceSectionY by remember { mutableIntStateOf(0) }
    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(contentPadding)
            .padding(bottom = Spacing.xxLarge),
    ) {
        issue.description?.let {
            Text(
                text = it,
                style = SnoutTheme.code.medium,
                color = SnoutTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.large, vertical = Spacing.small),
            )
        }
        TriageActions(
            status = issue.status,
            updatingTo = state.updatingTo,
            onChangeStatus = onChangeStatus,
            modifier = Modifier.padding(horizontal = Spacing.large, vertical = Spacing.small),
        )
        ImpactSection(issue)
        OverviewSection(
            issue = issue,
            assigneeName = issue.assignee?.let { assigneeName(it, state.members) },
            isAssigning = state.isAssigning,
            onAssigneeClicked = onAssigneeClicked,
        )
        Column(modifier = Modifier.onGloballyPositioned { occurrenceSectionY = it.positionInParent().y.toInt() }) {
            state.selectedOccurrence?.let { occurrence ->
                OccurrenceSection(
                    occurrence = occurrence,
                    exceptions = state.exceptions,
                    error = state.exceptionsError,
                    onRefresh = onRefresh,
                    onRetry = onExceptionsRetried,
                )
            }
        }
        // The stack trace above stands in for the top frame; it shows again when the trace can't.
        if (state.exceptions.isNullOrEmpty()) issue.topFrame?.let { LocationSection(it) }
        issue.latestRelease?.let { ReleaseSection(it) }
        RecentOccurrencesSection(
            occurrences = state.occurrences,
            error = state.occurrencesError,
            selectedOccurrenceId = state.selectedOccurrenceId,
            onRetry = onRefresh,
            onOccurrenceSelected = { id ->
                onOccurrenceSelected(id)
                scope.launch { scrollState.animateScrollTo(occurrenceSectionY) }
            },
        )
    }
}

/**
 * Resolve (the primary action) and Suppress while the issue is active; Reopen once it is anything else. Each change
 * is reversible from the snackbar that follows, so there is no confirmation step.
 */
@Composable
private fun TriageActions(
    status: IssueStatus,
    updatingTo: IssueStatusFilter?,
    onChangeStatus: (IssueStatusFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val idle = updatingTo == null
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small), modifier = modifier.fillMaxWidth()) {
        if (status == IssueStatus.Active) {
            PrimaryButton(
                text = stringResource(Res.string.error_tracking_action_resolve),
                onClick = { onChangeStatus(IssueStatusFilter.Resolved) },
                icon = painterResource(Res.drawable.ic_check_24dp),
                enabled = idle,
                loading = updatingTo == IssueStatusFilter.Resolved,
                modifier = Modifier.weight(1f),
            )
            SecondaryButton(
                text = stringResource(Res.string.error_tracking_action_suppress),
                onClick = { onChangeStatus(IssueStatusFilter.Suppressed) },
                enabled = idle,
                loading = updatingTo == IssueStatusFilter.Suppressed,
                modifier = Modifier.weight(1f),
            )
        } else {
            SecondaryButton(
                text = stringResource(Res.string.error_tracking_action_reopen),
                onClick = { onChangeStatus(IssueStatusFilter.Active) },
                enabled = idle,
                loading = updatingTo == IssueStatusFilter.Active,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ImpactSection(issue: ErrorTrackingIssue) {
    SectionLabel(stringResource(Res.string.error_tracking_impact_section))
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.large),
    ) {
        val cardModifier = Modifier.weight(1f)
        StatCard(stringResource(Res.string.error_tracking_occurrences), issue.occurrences.compact(), cardModifier)
        StatCard(stringResource(Res.string.error_tracking_users), issue.users.compact(), cardModifier)
        StatCard(stringResource(Res.string.error_tracking_sessions), issue.sessions.compact(), cardModifier)
    }
    if (issue.volume.isNotEmpty()) {
        BarChart(
            series = listOf(
                ChartSeries(
                    name = stringResource(Res.string.error_tracking_occurrences),
                    values = issue.volume.map { it.toDouble() },
                    color = chartColor(0),
                ),
            ),
            xLabels = lastDaysLabels(issue.volume.size),
            modifier = Modifier.padding(start = Spacing.large, end = Spacing.large, top = Spacing.large),
        )
    }
}

@Composable
private fun OverviewSection(
    issue: ErrorTrackingIssue,
    assigneeName: String?,
    isAssigning: Boolean,
    onAssigneeClicked: () -> Unit,
) {
    SectionLabel(stringResource(Res.string.error_tracking_overview_section))
    KeyValueRow(
        label = stringResource(Res.string.error_tracking_status_label),
        value = "",
        trailing = { IssueStatusPill(status = issue.status) },
    )
    issue.severity?.let {
        KeyValueRow(
            label = stringResource(Res.string.error_tracking_severity_label),
            value = "",
            trailing = { IssueSeverityTag(it) },
        )
    }
    KeyValueRow(
        label = stringResource(Res.string.error_tracking_assignee),
        value = assigneeName ?: stringResource(Res.string.error_tracking_unassigned),
        onClick = onAssigneeClicked.takeUnless { isAssigning },
        trailing = if (isAssigning) {
            { CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) }
        } else {
            null
        },
    )
    issue.lastSeen?.let {
        KeyValueRow(label = stringResource(Res.string.error_tracking_last_seen), value = it.formatRelative())
    }
    issue.firstSeen?.let {
        KeyValueRow(label = stringResource(Res.string.error_tracking_first_seen), value = it.formatDateTime())
    }
    issue.library?.let {
        KeyValueRow(label = stringResource(Res.string.error_tracking_library), value = it, valueIsCode = true)
    }
}

@Composable
private fun LocationSection(frame: StackFrame) {
    SectionLabel(stringResource(Res.string.error_tracking_location_section))
    frame.function?.let {
        KeyValueRow(label = stringResource(Res.string.error_tracking_function), value = it, valueIsCode = true)
    }
    frame.source?.let { source ->
        KeyValueRow(
            label = stringResource(Res.string.error_tracking_file),
            value = listOfNotNull(source, frame.line, frame.column).joinToString(":"),
            valueIsCode = true,
            valueMaxLines = 3,
        )
    }
}

@Composable
private fun ReleaseSection(release: IssueRelease) {
    SectionLabel(stringResource(Res.string.error_tracking_release_section))
    release.version?.let {
        KeyValueRow(label = stringResource(Res.string.error_tracking_release_version), value = it, valueIsCode = true)
    }
    release.commitId?.let {
        KeyValueRow(label = stringResource(Res.string.error_tracking_release_commit), value = it, valueIsCode = true)
    }
    release.branch?.let {
        KeyValueRow(label = stringResource(Res.string.error_tracking_release_branch), value = it, valueIsCode = true)
    }
}

/** A role assignee can't be picked here, so nothing is checked. */
@Composable
private fun AssigneePicker(
    isVisible: Boolean,
    members: List<OrganizationMember>,
    assignee: IssueAssignee?,
    onSelect: (Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    val unassigned = stringResource(Res.string.error_tracking_unassigned)
    OptionPickerSheet(
        isVisible = isVisible,
        title = stringResource(Res.string.error_tracking_assignee_picker_title),
        options = listOf(PickerOption(AssigneeChoice(null), unassigned)) +
            members.map {
                PickerOption(
                    AssigneeChoice(it.userId),
                    it.name,
                    it.email?.takeIf { email -> email != it.name },
                )
            },
        selected = when (assignee) {
            null -> AssigneeChoice(null)
            is IssueAssignee.User -> AssigneeChoice(assignee.userId)
            is IssueAssignee.Role -> null
        },
        onSelect = { onSelect(it.userId) },
        onDismiss = onDismiss,
    )
}

/** Wraps the user id so null means "Unassigned" rather than "nothing selected". */
private data class AssigneeChoice(val userId: Long?)

@Composable
private fun assigneeName(assignee: IssueAssignee, members: List<OrganizationMember>?): String = when (assignee) {
    is IssueAssignee.User -> members?.firstOrNull { it.userId == assignee.userId }?.name
        ?: stringResource(Res.string.error_tracking_assignee_member)

    is IssueAssignee.Role -> stringResource(Res.string.error_tracking_assignee_role)
}

private fun Long.compact(): String = toDouble().formatCompact()

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ErrorTrackingIssueScreenPreview() = ErrorTrackingIssueScreen(
    state = TestErrorTrackingIssueUiState,
    snackbarHostState = remember { SnackbarHostState() },
    onNavigateBack = {},
    onRefresh = {},
    onChangeStatus = {},
    onOpenInPostHog = {},
    onOccurrenceSelected = {},
    onExceptionsRetried = {},
    onAssigneeClicked = {},
    onAssigneeSelected = {},
    onAssigneePickerDismissed = {},
)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ErrorTrackingIssueScreenResolvedPreview() = ErrorTrackingIssueScreen(
    state = ErrorTrackingIssueUiState(issue = TestResolvedErrorTrackingIssue),
    snackbarHostState = remember { SnackbarHostState() },
    onNavigateBack = {},
    onRefresh = {},
    onChangeStatus = {},
    onOpenInPostHog = {},
    onOccurrenceSelected = {},
    onExceptionsRetried = {},
    onAssigneeClicked = {},
    onAssigneeSelected = {},
    onAssigneePickerDismissed = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ErrorTrackingIssueScreenForbiddenPreview() = ErrorTrackingIssueScreen(
    state = ErrorTrackingIssueUiState(error = DataError.Forbidden),
    snackbarHostState = remember { SnackbarHostState() },
    onNavigateBack = {},
    onRefresh = {},
    onChangeStatus = {},
    onOpenInPostHog = {},
    onOccurrenceSelected = {},
    onExceptionsRetried = {},
    onAssigneeClicked = {},
    onAssigneeSelected = {},
    onAssigneePickerDismissed = {},
)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ErrorTrackingIssueScreenResolvingPreview() = ErrorTrackingIssueScreen(
    state = TestErrorTrackingIssueUiState.copy(updatingTo = IssueStatusFilter.Resolved),
    snackbarHostState = remember { SnackbarHostState() },
    onNavigateBack = {},
    onRefresh = {},
    onChangeStatus = {},
    onOpenInPostHog = {},
    onOccurrenceSelected = {},
    onExceptionsRetried = {},
    onAssigneeClicked = {},
    onAssigneeSelected = {},
    onAssigneePickerDismissed = {},
)
