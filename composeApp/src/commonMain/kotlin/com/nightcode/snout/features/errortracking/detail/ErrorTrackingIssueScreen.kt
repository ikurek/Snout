package com.nightcode.snout.features.errortracking.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.compose.bottomOnly
import com.nightcode.snout.common.compose.withoutBottom
import com.nightcode.snout.common.format.formatCompact
import com.nightcode.snout.common.format.formatDateTime
import com.nightcode.snout.common.format.formatRelative
import com.nightcode.snout.common.format.lastDaysLabels
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.designsystem.atoms.SectionLabel
import com.nightcode.snout.designsystem.atoms.SkeletonRows
import com.nightcode.snout.designsystem.charts.BarChart
import com.nightcode.snout.designsystem.charts.ChartSeries
import com.nightcode.snout.designsystem.charts.chartColor
import com.nightcode.snout.designsystem.molecules.InfoBanner
import com.nightcode.snout.designsystem.molecules.KeyValueRow
import com.nightcode.snout.designsystem.molecules.StatCard
import com.nightcode.snout.designsystem.organisms.ErrorPlaceholder
import com.nightcode.snout.designsystem.organisms.ListScreenScaffold
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.features.errortracking.components.IssueSeverityTag
import com.nightcode.snout.features.errortracking.components.IssueStatusPill
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.errortracking.ErrorTrackingIssue
import com.nightcode.snout.shared.domain.model.errortracking.IssueRelease
import com.nightcode.snout.shared.domain.model.errortracking.StackFrame
import com.nightcode.snout.shared.domain.model.errortracking.TestErrorTrackingIssue
import com.nightcode.snout.shared.domain.model.errortracking.TestResolvedErrorTrackingIssue
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
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
import snout.composeapp.generated.resources.error_tracking_stack_trace_note
import snout.composeapp.generated.resources.error_tracking_status_label
import snout.composeapp.generated.resources.error_tracking_users

private const val LoadingRowCount = 4

@Composable
internal fun ErrorTrackingIssueScreen(
    state: CachedUiState<ErrorTrackingIssue>,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit,
    onOpenInPostHog: () -> Unit,
) {
    val issue = (state as? CachedUiState.Content)?.data
    ListScreenScaffold(
        title = issue?.name ?: stringResource(Res.string.error_tracking_issue_title_placeholder),
        onNavigateBack = onNavigateBack,
        onOpenInPostHog = onOpenInPostHog,
    ) { innerPadding ->
        val modifier = Modifier.fillMaxSize().padding(innerPadding.withoutBottom())
        when (state) {
            CachedUiState.Loading, is CachedUiState.Empty -> SkeletonRows(modifier = modifier, count = LoadingRowCount)
            is CachedUiState.Error -> ErrorPlaceholder(error = state.error, onRetry = onRetry, modifier = modifier)
            is CachedUiState.Content -> IssueContent(state.data, modifier, innerPadding.bottomOnly())
        }
    }
}

@Composable
private fun IssueContent(issue: ErrorTrackingIssue, modifier: Modifier, contentPadding: PaddingValues) {
    Column(modifier = modifier.verticalScroll(rememberScrollState()).padding(contentPadding).padding(bottom = 24.dp)) {
        issue.description?.let {
            Text(
                text = it,
                style = SnoutTheme.code.medium,
                color = SnoutTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        ImpactSection(issue)
        OverviewSection(issue)
        issue.topFrame?.let { LocationSection(it) }
        issue.latestRelease?.let { ReleaseSection(it) }
        InfoBanner(
            text = stringResource(Res.string.error_tracking_stack_trace_note),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp),
        )
    }
}

@Composable
private fun ImpactSection(issue: ErrorTrackingIssue) {
    SectionLabel(stringResource(Res.string.error_tracking_impact_section))
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
        )
    }
}

@Composable
private fun OverviewSection(issue: ErrorTrackingIssue) {
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

private fun Long.compact(): String = toDouble().formatCompact()

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ErrorTrackingIssueScreenPreview() = ErrorTrackingIssueScreen(
    state = CachedUiState.Content(TestErrorTrackingIssue, isRefreshing = false),
    onNavigateBack = {},
    onRetry = {},
    onOpenInPostHog = {},
)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ErrorTrackingIssueScreenResolvedPreview() = ErrorTrackingIssueScreen(
    state = CachedUiState.Content(TestResolvedErrorTrackingIssue, isRefreshing = false),
    onNavigateBack = {},
    onRetry = {},
    onOpenInPostHog = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ErrorTrackingIssueScreenForbiddenPreview() = ErrorTrackingIssueScreen(
    state = CachedUiState.Error(DataError.Forbidden),
    onNavigateBack = {},
    onRetry = {},
    onOpenInPostHog = {},
)
