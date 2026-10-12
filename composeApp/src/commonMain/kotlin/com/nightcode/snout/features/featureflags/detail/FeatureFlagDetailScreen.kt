package com.nightcode.snout.features.featureflags.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.compose.bottomOnly
import com.nightcode.snout.common.compose.withoutBottom
import com.nightcode.snout.common.state.LoadState
import com.nightcode.snout.common.state.dataOrNull
import com.nightcode.snout.designsystem.actionFailedMessage
import com.nightcode.snout.designsystem.atoms.SectionLabel
import com.nightcode.snout.designsystem.molecules.InlineErrorBanner
import com.nightcode.snout.designsystem.organisms.ConfirmationDialog
import com.nightcode.snout.designsystem.organisms.LoadStateContainer
import com.nightcode.snout.designsystem.organisms.PushedScreenScaffold
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.features.featureflags.detail.components.ConditionCard
import com.nightcode.snout.features.featureflags.detail.components.DetailsSection
import com.nightcode.snout.features.featureflags.detail.components.PayloadSection
import com.nightcode.snout.features.featureflags.detail.components.ToggleCard
import com.nightcode.snout.features.featureflags.detail.components.VariantsSection
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.featureflags.FeatureFlag
import com.nightcode.snout.shared.domain.model.featureflags.TestRemoteConfigFeatureFlag
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.feature_flag_conditions_section
import snout.composeapp.generated.resources.feature_flag_dialog_confirm_disable
import snout.composeapp.generated.resources.feature_flag_dialog_confirm_enable
import snout.composeapp.generated.resources.feature_flag_dialog_text_disable
import snout.composeapp.generated.resources.feature_flag_dialog_text_enable
import snout.composeapp.generated.resources.feature_flag_dialog_title_disable
import snout.composeapp.generated.resources.feature_flag_dialog_title_enable
import snout.composeapp.generated.resources.feature_flag_title_placeholder
import snout.composeapp.generated.resources.feature_flag_toggle_error_forbidden
import snout.composeapp.generated.resources.feature_flag_toggle_error_generic
import snout.composeapp.generated.resources.feature_flag_toggle_error_rate_limited

@Composable
internal fun FeatureFlagDetailScreen(
    state: FeatureFlagDetailUiState,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onToggleConfirmed: (active: Boolean) -> Unit,
    onToggleErrorDismissed: () -> Unit,
    onOpenInPostHog: () -> Unit,
) {
    var pendingToggle by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val flag = state.flag.dataOrNull

    PushedScreenScaffold(
        title = flag?.key ?: stringResource(Res.string.feature_flag_title_placeholder),
        onNavigateBack = onNavigateBack,
        titleStyle = SnoutTheme.code.large,
        onOpenInPostHog = onOpenInPostHog,
    ) { innerPadding ->
        LoadStateContainer(
            state = state.flag,
            onRefresh = onRefresh,
            modifier = Modifier.padding(innerPadding.withoutBottom()),
        ) { flag ->
            FlagContent(
                flag = flag,
                toggle = state.toggle,
                onToggleRequest = { pendingToggle = it },
                onToggleErrorDismissed = onToggleErrorDismissed,
                modifier = Modifier.fillMaxSize(),
                contentPadding = innerPadding.bottomOnly(),
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
    onToggleErrorDismissed: () -> Unit,
    modifier: Modifier,
    contentPadding: PaddingValues,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(top = Spacing.small, bottom = Spacing.xxLarge),
    ) {
        ToggleCard(
            flag = flag,
            toggle = toggle,
            onToggleRequest = onToggleRequest,
            modifier = Modifier.padding(horizontal = Spacing.large),
        )
        if (toggle is ToggleStatus.Failed) {
            InlineErrorBanner(
                text = stringResource(
                    toggle.error.actionFailedMessage(
                        generic = Res.string.feature_flag_toggle_error_generic,
                        forbidden = Res.string.feature_flag_toggle_error_forbidden,
                        rateLimited = Res.string.feature_flag_toggle_error_rate_limited,
                    ),
                ),
                onAction = onToggleErrorDismissed,
                modifier = Modifier.padding(start = Spacing.large, end = Spacing.large, top = Spacing.medium),
            )
        }
        flag.description?.let {
            Text(
                text = it,
                style = SnoutTheme.typography.bodyMedium,
                color = SnoutTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(start = Spacing.large, end = Spacing.large, top = Spacing.medium),
            )
        }

        PayloadSection(flag)
        // Remote configuration flags serve their value to everyone; PostHog hides release conditions for them too.
        if (flag.isRemoteConfiguration.not()) {
            SectionLabel(stringResource(Res.string.feature_flag_conditions_section))
            Column(
                modifier = Modifier.padding(horizontal = Spacing.large),
                verticalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                flag.conditions.forEachIndexed { index, condition ->
                    ConditionCard(
                        index = index,
                        condition = condition,
                    )
                }
            }
        }
        if (flag.isMultivariate) {
            VariantsSection(flag)
        }
        DetailsSection(flag)
    }
}

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun FeatureFlagDetailScreenPreview() = FeatureFlagDetailScreen(
    state = TestFeatureFlagDetailUiState,
    onNavigateBack = {},
    onRefresh = {},
    onToggleConfirmed = {},
    onToggleErrorDismissed = {},
    onOpenInPostHog = {},
)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun FeatureFlagDetailScreenForbiddenPreview() = FeatureFlagDetailScreen(
    state = TestFeatureFlagDetailUiState.copy(toggle = ToggleStatus.Failed(DataError.Forbidden)),
    onNavigateBack = {},
    onRefresh = {},
    onToggleConfirmed = {},
    onToggleErrorDismissed = {},
    onOpenInPostHog = {},
)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun FeatureFlagDetailScreenRemoteConfigPreview() = FeatureFlagDetailScreen(
    state = FeatureFlagDetailUiState(flag = LoadState.Loaded(TestRemoteConfigFeatureFlag)),
    onNavigateBack = {},
    onRefresh = {},
    onToggleConfirmed = {},
    onToggleErrorDismissed = {},
    onOpenInPostHog = {},
)
