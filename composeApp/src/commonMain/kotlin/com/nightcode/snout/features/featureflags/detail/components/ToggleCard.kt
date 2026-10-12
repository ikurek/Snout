package com.nightcode.snout.features.featureflags.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nightcode.snout.designsystem.molecules.InfoBanner
import com.nightcode.snout.designsystem.molecules.TonalCard
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.features.featureflags.components.FeatureFlagStatusPill
import com.nightcode.snout.features.featureflags.components.rolloutSummary
import com.nightcode.snout.features.featureflags.detail.ToggleStatus
import com.nightcode.snout.shared.domain.model.featureflags.FeatureFlag
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.feature_flag_enabled_label
import snout.composeapp.generated.resources.feature_flag_read_only_note

/** "Enabled" card: label and rollout summary on the left, status pill and switch on the right. */
@Composable
internal fun ToggleCard(
    flag: FeatureFlag,
    toggle: ToggleStatus,
    onToggleRequest: (active: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    TonalCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.feature_flag_enabled_label),
                    style = SnoutTheme.typography.titleSmall,
                )
                Text(
                    text = flag.rolloutSummary(),
                    style = SnoutTheme.typography.bodySmall,
                    color = SnoutTheme.colors.onSurfaceVariant,
                )
            }
            FeatureFlagStatusPill(status = flag.status)
            if (toggle is ToggleStatus.Updating) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Switch(checked = flag.active, onCheckedChange = onToggleRequest, enabled = flag.canEdit)
            }
        }
        if (flag.canEdit.not()) {
            InfoBanner(
                text = stringResource(Res.string.feature_flag_read_only_note),
                modifier = Modifier.padding(top = Spacing.small),
            )
        }
    }
}
