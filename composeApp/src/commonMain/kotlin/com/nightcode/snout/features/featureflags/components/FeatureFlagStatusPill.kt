package com.nightcode.snout.features.featureflags.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.atoms.StatusPill
import com.nightcode.snout.designsystem.atoms.StatusTone
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.shared.domain.model.featureflags.FeatureFlagStatus
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.feature_flag_status_active
import snout.composeapp.generated.resources.feature_flag_status_inactive
import snout.composeapp.generated.resources.feature_flag_status_stale
import snout.composeapp.generated.resources.feature_flag_status_unknown

@Composable
internal fun FeatureFlagStatusPill(status: FeatureFlagStatus) {
    val (label, tone) = when (status) {
        FeatureFlagStatus.Active -> Res.string.feature_flag_status_active to StatusTone.Active
        FeatureFlagStatus.Inactive -> Res.string.feature_flag_status_inactive to StatusTone.Neutral
        FeatureFlagStatus.Stale -> Res.string.feature_flag_status_stale to StatusTone.Info
        FeatureFlagStatus.Unknown -> Res.string.feature_flag_status_unknown to StatusTone.Neutral
    }
    StatusPill(text = stringResource(label), tone = tone)
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun FeatureFlagStatusPillPreview() = FlowRow(
    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
    verticalArrangement = Arrangement.spacedBy(Spacing.small),
) {
    FeatureFlagStatus.entries.forEach { FeatureFlagStatusPill(status = it) }
}
