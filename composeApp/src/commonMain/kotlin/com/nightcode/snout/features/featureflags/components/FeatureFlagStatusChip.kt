package com.nightcode.snout.features.featureflags.components

import androidx.compose.runtime.Composable
import com.nightcode.snout.designsystem.atoms.LabelChip
import com.nightcode.snout.designsystem.atoms.LabelChipTone
import com.nightcode.snout.shared.domain.model.featureflags.FeatureFlag
import com.nightcode.snout.shared.domain.model.featureflags.FeatureFlagStatus
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.feature_flag_rollout_conditions
import snout.composeapp.generated.resources.feature_flag_rollout_everyone
import snout.composeapp.generated.resources.feature_flag_rollout_percent
import snout.composeapp.generated.resources.feature_flag_rollout_remote_config
import snout.composeapp.generated.resources.feature_flag_rollout_variants
import snout.composeapp.generated.resources.feature_flag_status_active
import snout.composeapp.generated.resources.feature_flag_status_inactive
import snout.composeapp.generated.resources.feature_flag_status_stale
import snout.composeapp.generated.resources.feature_flag_status_unknown

@Composable
internal fun FeatureFlagStatusChip(status: FeatureFlagStatus) {
    val (label, tone) = when (status) {
        FeatureFlagStatus.Active -> Res.string.feature_flag_status_active to LabelChipTone.Success
        FeatureFlagStatus.Inactive -> Res.string.feature_flag_status_inactive to LabelChipTone.Neutral
        FeatureFlagStatus.Stale -> Res.string.feature_flag_status_stale to LabelChipTone.Warning
        FeatureFlagStatus.Unknown -> Res.string.feature_flag_status_unknown to LabelChipTone.Neutral
    }
    LabelChip(text = stringResource(label), tone = tone)
}

/** One line describing who gets the flag. */
@Composable
internal fun FeatureFlag.rolloutSummary(): String = when {
    isRemoteConfiguration -> stringResource(Res.string.feature_flag_rollout_remote_config)
    isMultivariate -> pluralStringResource(Res.plurals.feature_flag_rollout_variants, variants.size, variants.size)
    isReleasedToEveryone -> stringResource(Res.string.feature_flag_rollout_everyone)
    conditions.size == 1 -> stringResource(Res.string.feature_flag_rollout_percent, conditions[0].rolloutPercentage)
    else -> pluralStringResource(Res.plurals.feature_flag_rollout_conditions, conditions.size, conditions.size)
}
