package com.nightcode.snout.features.errortracking.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.atoms.StatusPill
import com.nightcode.snout.designsystem.atoms.StatusTone
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatus
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.error_tracking_status_active
import snout.composeapp.generated.resources.error_tracking_status_archived
import snout.composeapp.generated.resources.error_tracking_status_pending_release
import snout.composeapp.generated.resources.error_tracking_status_resolved
import snout.composeapp.generated.resources.error_tracking_status_suppressed
import snout.composeapp.generated.resources.error_tracking_status_unknown

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

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun IssueStatusPillPreview() = FlowRow(
    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
    verticalArrangement = Arrangement.spacedBy(Spacing.small),
) {
    IssueStatus.entries.forEach { IssueStatusPill(status = it) }
}
