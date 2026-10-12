package com.nightcode.snout.features.featureflags.detail.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.feature_flag_rollout_percent

@Composable
internal fun RolloutPercent(percent: Int) {
    Text(
        text = stringResource(Res.string.feature_flag_rollout_percent, percent),
        style = SnoutTheme.typography.titleSmall,
    )
}
