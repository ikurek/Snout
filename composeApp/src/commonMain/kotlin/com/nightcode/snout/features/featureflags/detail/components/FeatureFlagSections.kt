package com.nightcode.snout.features.featureflags.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nightcode.snout.common.format.formatShortDate
import com.nightcode.snout.designsystem.atoms.SectionLabel
import com.nightcode.snout.designsystem.atoms.VerticalSpacer
import com.nightcode.snout.designsystem.molecules.CodeBlock
import com.nightcode.snout.designsystem.molecules.InfoBanner
import com.nightcode.snout.designsystem.molecules.KeyValueRow
import com.nightcode.snout.designsystem.molecules.TagList
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.shared.domain.model.featureflags.FeatureFlag
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.feature_flag_created_by
import snout.composeapp.generated.resources.feature_flag_created_on
import snout.composeapp.generated.resources.feature_flag_details_section
import snout.composeapp.generated.resources.feature_flag_payload_section
import snout.composeapp.generated.resources.feature_flag_tags
import snout.composeapp.generated.resources.feature_flag_value_encrypted_note
import snout.composeapp.generated.resources.feature_flag_value_read_only_note
import snout.composeapp.generated.resources.feature_flag_value_section
import snout.composeapp.generated.resources.feature_flag_variant_payload
import snout.composeapp.generated.resources.feature_flag_variants_section

/** The value a boolean or remote configuration flag serves. Shown read-only; editing payloads is a PostHog web task. */
@Composable
internal fun PayloadSection(flag: FeatureFlag) {
    val payload = flag.booleanPayload ?: return
    val title = if (flag.isRemoteConfiguration) {
        Res.string.feature_flag_value_section
    } else {
        Res.string.feature_flag_payload_section
    }
    SectionLabel(stringResource(title))
    CodeBlock(text = payload, modifier = Modifier.padding(horizontal = Spacing.large))
    VerticalSpacer(Spacing.medium)
    val note = if (flag.hasEncryptedPayloads) {
        Res.string.feature_flag_value_encrypted_note
    } else {
        Res.string.feature_flag_value_read_only_note
    }
    InfoBanner(text = stringResource(note), modifier = Modifier.padding(horizontal = Spacing.large))
}

@Composable
internal fun VariantsSection(flag: FeatureFlag) {
    SectionLabel(stringResource(Res.string.feature_flag_variants_section))
    flag.variants.forEach { variant ->
        KeyValueRow(
            label = variant.name?.takeIf { it.isNotBlank() } ?: variant.key,
            value = variant.key,
            valueIsCode = true,
            trailing = { RolloutPercent(variant.rolloutPercentage) },
        )
        flag.variantPayload(variant.key)?.let { payload ->
            VariantPayload(
                payload = payload,
                modifier = Modifier.padding(horizontal = Spacing.large).padding(bottom = Spacing.small),
            )
        }
    }
    if (flag.payloads.isNotEmpty()) {
        InfoBanner(
            text = stringResource(Res.string.feature_flag_value_read_only_note),
            modifier = Modifier.padding(start = Spacing.large, end = Spacing.large, top = Spacing.xxSmall),
        )
    }
}

@Composable
private fun VariantPayload(payload: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.xSmall)) {
        Text(
            text = stringResource(Res.string.feature_flag_variant_payload),
            style = SnoutTheme.typography.bodySmall,
            color = SnoutTheme.colors.onSurfaceVariant,
        )
        CodeBlock(text = payload)
    }
}

@Composable
internal fun DetailsSection(flag: FeatureFlag) {
    SectionLabel(stringResource(Res.string.feature_flag_details_section))
    flag.createdBy?.let { KeyValueRow(label = stringResource(Res.string.feature_flag_created_by), value = it) }
    flag.createdAt?.let {
        KeyValueRow(label = stringResource(Res.string.feature_flag_created_on), value = it.formatShortDate())
    }
    if (flag.tags.isNotEmpty()) {
        TagList(label = stringResource(Res.string.feature_flag_tags), tags = flag.tags)
    }
}
