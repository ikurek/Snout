package com.nightcode.snout.features.featureflags.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.nightcode.snout.designsystem.molecules.CodeChip
import com.nightcode.snout.designsystem.molecules.TonalCard
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.shared.domain.model.featureflags.ReleaseCondition
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.feature_flag_condition_everyone
import snout.composeapp.generated.resources.feature_flag_condition_title
import snout.composeapp.generated.resources.feature_flag_condition_variant

@Composable
internal fun ConditionCard(index: Int, condition: ReleaseCondition, modifier: Modifier = Modifier) {
    TonalCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.feature_flag_condition_title, index + 1),
                style = SnoutTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            RolloutPercent(condition.rolloutPercentage)
        }
        if (condition.properties.isEmpty()) {
            Text(
                text = stringResource(Res.string.feature_flag_condition_everyone),
                style = SnoutTheme.typography.bodySmall,
                color = SnoutTheme.colors.onSurfaceVariant,
            )
        } else {
            condition.properties.forEach { property ->
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xSmall),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xxSmall),
                ) {
                    CodeChip(text = property.key)
                    property.operator?.let {
                        Text(
                            text = it,
                            style = SnoutTheme.typography.bodySmall,
                            color = SnoutTheme.colors.onSurfaceVariant,
                        )
                    }
                    CodeChip(text = property.value)
                }
            }
        }
        condition.variant?.let {
            Text(
                text = stringResource(Res.string.feature_flag_condition_variant, it),
                style = SnoutTheme.typography.bodySmall,
                color = SnoutTheme.colors.onSurfaceVariant,
            )
        }
    }
}
