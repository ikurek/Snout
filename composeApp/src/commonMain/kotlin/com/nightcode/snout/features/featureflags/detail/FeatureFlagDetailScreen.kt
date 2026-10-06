package com.nightcode.snout.features.featureflags.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.format.formatShortDate
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.designsystem.SectionTitle
import com.nightcode.snout.designsystem.atoms.buttons.NavigationBackIconButton
import com.nightcode.snout.designsystem.atoms.buttons.OpenInPostHogIconButton
import com.nightcode.snout.designsystem.molecules.CodeBlock
import com.nightcode.snout.designsystem.InfoNote
import com.nightcode.snout.designsystem.InlineError
import com.nightcode.snout.designsystem.molecules.KeyValueRow
import com.nightcode.snout.designsystem.molecules.SectionCard
import com.nightcode.snout.designsystem.molecules.TagList
import com.nightcode.snout.designsystem.organisms.ConfirmationDialog
import com.nightcode.snout.designsystem.organisms.ErrorPlaceholder
import com.nightcode.snout.designsystem.LoadingIndicator
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.features.featureflags.components.FeatureFlagStatusChip
import com.nightcode.snout.features.featureflags.components.rolloutSummary
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.featureflags.FeatureFlag
import com.nightcode.snout.shared.domain.model.featureflags.ReleaseCondition
import com.nightcode.snout.shared.domain.model.featureflags.TestRemoteConfigFeatureFlag
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.feature_flag_condition_everyone
import snout.composeapp.generated.resources.feature_flag_condition_title
import snout.composeapp.generated.resources.feature_flag_condition_variant
import snout.composeapp.generated.resources.feature_flag_conditions_section
import snout.composeapp.generated.resources.feature_flag_created_by
import snout.composeapp.generated.resources.feature_flag_created_on
import snout.composeapp.generated.resources.feature_flag_details_section
import snout.composeapp.generated.resources.feature_flag_dialog_confirm_disable
import snout.composeapp.generated.resources.feature_flag_dialog_confirm_enable
import snout.composeapp.generated.resources.feature_flag_dialog_text_disable
import snout.composeapp.generated.resources.feature_flag_dialog_text_enable
import snout.composeapp.generated.resources.feature_flag_dialog_title_disable
import snout.composeapp.generated.resources.feature_flag_dialog_title_enable
import snout.composeapp.generated.resources.feature_flag_enabled_label
import snout.composeapp.generated.resources.feature_flag_payload_section
import snout.composeapp.generated.resources.feature_flag_read_only_note
import snout.composeapp.generated.resources.feature_flag_rollout_percent
import snout.composeapp.generated.resources.feature_flag_tags
import snout.composeapp.generated.resources.feature_flag_title_placeholder
import snout.composeapp.generated.resources.feature_flag_toggle_error_forbidden
import snout.composeapp.generated.resources.feature_flag_toggle_error_generic
import snout.composeapp.generated.resources.feature_flag_toggle_error_rate_limited
import snout.composeapp.generated.resources.feature_flag_value_encrypted_note
import snout.composeapp.generated.resources.feature_flag_value_read_only_note
import snout.composeapp.generated.resources.feature_flag_value_section
import snout.composeapp.generated.resources.feature_flag_variant_payload
import snout.composeapp.generated.resources.feature_flag_variants_section

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FeatureFlagDetailScreen(
    state: FeatureFlagDetailUiState,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit,
    onToggleConfirmed: (active: Boolean) -> Unit,
    onDismissToggleError: () -> Unit,
    onOpenInPostHog: () -> Unit,
) {
    var pendingToggle by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val flag = (state.flag as? CachedUiState.Content)?.data

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = flag?.key ?: stringResource(Res.string.feature_flag_title_placeholder),
                        style = SnoutTheme.code.large,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = { NavigationBackIconButton(onClick = onNavigateBack) },
                actions = { OpenInPostHogIconButton(onClick = onOpenInPostHog) },
            )
        },
    ) { innerPadding ->
        val modifier = Modifier.fillMaxSize().padding(innerPadding)
        when (val flagState = state.flag) {
            CachedUiState.Loading, is CachedUiState.Empty -> LoadingIndicator(modifier)

            is CachedUiState.Error -> ErrorPlaceholder(error = flagState.error, onRetry = onRetry, modifier = modifier)

            is CachedUiState.Content -> FlagContent(
                flag = flagState.data,
                toggle = state.toggle,
                onToggleRequest = { pendingToggle = it },
                onDismissToggleError = onDismissToggleError,
                modifier = modifier,
            )
        }
    }

    pendingToggle?.let { enable ->
        val texts = toggleDialogTexts(enable)
        ConfirmationDialog(
            title = stringResource(texts.title),
            text = stringResource(texts.text, flag?.key.orEmpty()),
            confirmLabel = stringResource(texts.confirm),
            destructive = enable.not(),
            onConfirm = {
                pendingToggle = null
                onToggleConfirmed(enable)
            },
            onDismiss = { pendingToggle = null },
        )
    }
}

private data class ToggleDialogTexts(val title: StringResource, val text: StringResource, val confirm: StringResource)

private fun toggleDialogTexts(enable: Boolean) = if (enable) {
    ToggleDialogTexts(
        title = Res.string.feature_flag_dialog_title_enable,
        text = Res.string.feature_flag_dialog_text_enable,
        confirm = Res.string.feature_flag_dialog_confirm_enable,
    )
} else {
    ToggleDialogTexts(
        title = Res.string.feature_flag_dialog_title_disable,
        text = Res.string.feature_flag_dialog_text_disable,
        confirm = Res.string.feature_flag_dialog_confirm_disable,
    )
}

@Composable
private fun FlagContent(
    flag: FeatureFlag,
    toggle: ToggleStatus,
    onToggleRequest: (active: Boolean) -> Unit,
    onDismissToggleError: () -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ToggleCard(flag = flag, toggle = toggle, onToggleRequest = onToggleRequest)
        if (toggle is ToggleStatus.Failed) {
            InlineError(
                text = stringResource(toggle.error.toggleMessage()),
                onRetry = onDismissToggleError,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        flag.description?.let {
            Text(
                text = it,
                style = SnoutTheme.typography.bodyMedium,
                color = SnoutTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        PayloadSection(flag)
        // Remote configuration flags serve their value to everyone; PostHog hides release conditions for them too.
        if (flag.isRemoteConfiguration.not()) {
            SectionTitle(stringResource(Res.string.feature_flag_conditions_section))
            flag.conditions.forEachIndexed { index, condition ->
                ConditionCard(index = index, condition = condition, modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
        if (flag.isMultivariate) {
            VariantsSection(flag)
        }
        DetailsSection(flag)
    }
}

/** The value a boolean or remote configuration flag serves. Shown read-only; editing payloads is a PostHog web task. */
@Composable
private fun PayloadSection(flag: FeatureFlag) {
    val payload = flag.booleanPayload ?: return
    val title = if (flag.isRemoteConfiguration) {
        Res.string.feature_flag_value_section
    } else {
        Res.string.feature_flag_payload_section
    }
    SectionTitle(stringResource(title))
    CodeBlock(text = payload, modifier = Modifier.padding(horizontal = 16.dp))
    val note = if (flag.hasEncryptedPayloads) {
        Res.string.feature_flag_value_encrypted_note
    } else {
        Res.string.feature_flag_value_read_only_note
    }
    InfoNote(text = stringResource(note), modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
private fun VariantsSection(flag: FeatureFlag) {
    SectionTitle(stringResource(Res.string.feature_flag_variants_section))
    flag.variants.forEach { variant ->
        KeyValueRow(
            label = variant.name?.takeIf { it.isNotBlank() } ?: variant.key,
            value = variant.key,
            trailing = {
                Text(
                    text = stringResource(Res.string.feature_flag_rollout_percent, variant.rolloutPercentage),
                    style = SnoutTheme.code.medium,
                )
            },
        )
        flag.variantPayload(variant.key)?.let { payload ->
            VariantPayload(payload = payload, modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp))
        }
    }
    if (flag.payloads.isNotEmpty()) {
        InfoNote(
            text = stringResource(Res.string.feature_flag_value_read_only_note),
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

@Composable
private fun VariantPayload(payload: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(Res.string.feature_flag_variant_payload),
            style = SnoutTheme.typography.labelMedium,
            color = SnoutTheme.colors.onSurfaceVariant,
        )
        CodeBlock(text = payload)
    }
}

@Composable
private fun DetailsSection(flag: FeatureFlag) {
    SectionTitle(stringResource(Res.string.feature_flag_details_section))
    flag.createdBy?.let { KeyValueRow(label = stringResource(Res.string.feature_flag_created_by), value = it) }
    flag.createdAt?.let {
        KeyValueRow(label = stringResource(Res.string.feature_flag_created_on), value = it.formatShortDate())
    }
    if (flag.tags.isNotEmpty()) {
        TagList(label = stringResource(Res.string.feature_flag_tags), tags = flag.tags)
    }
}

@Composable
private fun ToggleCard(flag: FeatureFlag, toggle: ToggleStatus, onToggleRequest: (active: Boolean) -> Unit) {
    SectionCard(
        title = stringResource(Res.string.feature_flag_enabled_label),
        subtitle = flag.rolloutSummary(),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureFlagStatusChip(status = flag.status)
                if (toggle is ToggleStatus.Updating) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Switch(checked = flag.active, onCheckedChange = onToggleRequest, enabled = flag.canEdit)
                }
            }
        },
    ) {
        if (flag.canEdit.not()) {
            InfoNote(text = stringResource(Res.string.feature_flag_read_only_note))
        }
    }
}

@Composable
private fun ConditionCard(index: Int, condition: ReleaseCondition, modifier: Modifier = Modifier) {
    SectionCard(
        title = stringResource(Res.string.feature_flag_condition_title, index + 1),
        modifier = modifier,
        trailing = {
            Text(
                text = stringResource(Res.string.feature_flag_rollout_percent, condition.rolloutPercentage),
                style = SnoutTheme.code.medium,
                color = SnoutTheme.colors.primary,
            )
        },
    ) {
        if (condition.properties.isEmpty()) {
            Text(
                text = stringResource(Res.string.feature_flag_condition_everyone),
                style = SnoutTheme.typography.bodySmall,
                color = SnoutTheme.colors.onSurfaceVariant,
            )
        } else {
            condition.properties.forEach { property ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = property.key, style = SnoutTheme.code.small)
                    property.operator?.let {
                        Text(
                            text = it,
                            style = SnoutTheme.typography.bodySmall,
                            color = SnoutTheme.colors.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = property.value,
                        style = SnoutTheme.code.small,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        condition.variant?.let {
            Text(
                text = stringResource(Res.string.feature_flag_condition_variant, it),
                style = SnoutTheme.typography.labelSmall,
                color = SnoutTheme.colors.onSurfaceVariant,
            )
        }
    }
}

private fun DataError.toggleMessage() = when (this) {
    DataError.Forbidden -> Res.string.feature_flag_toggle_error_forbidden

    DataError.RateLimited -> Res.string.feature_flag_toggle_error_rate_limited

    DataError.NotFound, DataError.PaymentRequired, DataError.ConnectionError, DataError.UnexpectedError ->
        Res.string.feature_flag_toggle_error_generic
}

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun FeatureFlagDetailScreenPreview() = FeatureFlagDetailScreen(
    state = TestFeatureFlagDetailUiState,
    onNavigateBack = {},
    onRetry = {},
    onToggleConfirmed = {},
    onDismissToggleError = {},
    onOpenInPostHog = {},
)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun FeatureFlagDetailScreenForbiddenPreview() = FeatureFlagDetailScreen(
    state = TestFeatureFlagDetailUiState.copy(toggle = ToggleStatus.Failed(DataError.Forbidden)),
    onNavigateBack = {},
    onRetry = {},
    onToggleConfirmed = {},
    onDismissToggleError = {},
    onOpenInPostHog = {},
)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun FeatureFlagDetailScreenRemoteConfigPreview() = FeatureFlagDetailScreen(
    state = FeatureFlagDetailUiState(flag = CachedUiState.Content(TestRemoteConfigFeatureFlag, isRefreshing = false)),
    onNavigateBack = {},
    onRetry = {},
    onToggleConfirmed = {},
    onDismissToggleError = {},
    onOpenInPostHog = {},
)
