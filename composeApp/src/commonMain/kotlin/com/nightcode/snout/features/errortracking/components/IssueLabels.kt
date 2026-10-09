package com.nightcode.snout.features.errortracking.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nightcode.snout.designsystem.atoms.StatusPill
import com.nightcode.snout.designsystem.atoms.StatusTone
import com.nightcode.snout.designsystem.atoms.Tag
import com.nightcode.snout.designsystem.molecules.Placeholder
import com.nightcode.snout.shared.domain.model.errortracking.IssueSeverity
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatus
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.error_tracking_severity_critical
import snout.composeapp.generated.resources.error_tracking_severity_high
import snout.composeapp.generated.resources.error_tracking_severity_low
import snout.composeapp.generated.resources.error_tracking_severity_medium
import snout.composeapp.generated.resources.error_tracking_status_active
import snout.composeapp.generated.resources.error_tracking_status_archived
import snout.composeapp.generated.resources.error_tracking_status_pending_release
import snout.composeapp.generated.resources.error_tracking_status_resolved
import snout.composeapp.generated.resources.error_tracking_status_suppressed
import snout.composeapp.generated.resources.error_tracking_status_unknown
import snout.composeapp.generated.resources.error_tracking_unavailable_subtitle
import snout.composeapp.generated.resources.error_tracking_unavailable_title
import snout.composeapp.generated.resources.ic_info_outlined_24dp
import snout.composeapp.generated.resources.placeholder_generic_error_retry

/** Active issues are the ones that need attention, so they take the attention tone rather than "on" green. */
@Composable
internal fun IssueStatusPill(status: IssueStatus) {
    val (label, tone) = when (status) {
        IssueStatus.Active -> Res.string.error_tracking_status_active to StatusTone.Attention
        IssueStatus.Resolved -> Res.string.error_tracking_status_resolved to StatusTone.Active
        IssueStatus.PendingRelease -> Res.string.error_tracking_status_pending_release to StatusTone.Info
        IssueStatus.Suppressed -> Res.string.error_tracking_status_suppressed to StatusTone.Neutral
        IssueStatus.Archived -> Res.string.error_tracking_status_archived to StatusTone.Neutral
        IssueStatus.Unknown -> Res.string.error_tracking_status_unknown to StatusTone.Neutral
    }
    StatusPill(text = stringResource(label), tone = tone)
}

@Composable
internal fun IssueSeverityTag(severity: IssueSeverity, modifier: Modifier = Modifier) {
    val tone = when (severity) {
        IssueSeverity.High, IssueSeverity.Critical -> StatusTone.Attention
        IssueSeverity.Low, IssueSeverity.Medium -> StatusTone.Neutral
    }
    Tag(text = stringResource(severity.label), tone = tone, modifier = modifier)
}

internal val IssueSeverity.label: StringResource
    get() = when (this) {
        IssueSeverity.Low -> Res.string.error_tracking_severity_low
        IssueSeverity.Medium -> Res.string.error_tracking_severity_medium
        IssueSeverity.High -> Res.string.error_tracking_severity_high
        IssueSeverity.Critical -> Res.string.error_tracking_severity_critical
    }

internal val IssueStatusFilter.label: StringResource
    get() = when (this) {
        IssueStatusFilter.Active -> Res.string.error_tracking_status_active
        IssueStatusFilter.Resolved -> Res.string.error_tracking_status_resolved
        IssueStatusFilter.Suppressed -> Res.string.error_tracking_status_suppressed
    }

/** PostHog answers 404 for the whole error tracking API on instances that predate it. */
@Composable
internal fun ErrorTrackingUnavailablePlaceholder(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Placeholder(
        icon = painterResource(Res.drawable.ic_info_outlined_24dp),
        title = stringResource(Res.string.error_tracking_unavailable_title),
        message = stringResource(Res.string.error_tracking_unavailable_subtitle),
        actionLabel = stringResource(Res.string.placeholder_generic_error_retry),
        onAction = onRetry,
        modifier = modifier,
    )
}
