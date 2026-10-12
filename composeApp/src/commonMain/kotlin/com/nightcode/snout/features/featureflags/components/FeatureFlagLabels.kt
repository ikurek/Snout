package com.nightcode.snout.features.featureflags.components

import androidx.compose.runtime.Composable
import com.nightcode.snout.shared.domain.model.featureflags.FeatureFlag
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.feature_flag_rollout_conditions
import snout.composeapp.generated.resources.feature_flag_rollout_everyone
import snout.composeapp.generated.resources.feature_flag_rollout_percent
import snout.composeapp.generated.resources.feature_flag_rollout_remote_config
import snout.composeapp.generated.resources.feature_flag_rollout_variants

/** One line describing who gets the flag. */
@Composable
internal fun FeatureFlag.rolloutSummary(): String = when {
    isRemoteConfiguration -> stringResource(Res.string.feature_flag_rollout_remote_config)
    isMultivariate -> pluralStringResource(Res.plurals.feature_flag_rollout_variants, variants.size, variants.size)
    isReleasedToEveryone -> stringResource(Res.string.feature_flag_rollout_everyone)
    conditions.size == 1 -> stringResource(Res.string.feature_flag_rollout_percent, conditions[0].rolloutPercentage)
    else -> pluralStringResource(Res.plurals.feature_flag_rollout_conditions, conditions.size, conditions.size)
}
