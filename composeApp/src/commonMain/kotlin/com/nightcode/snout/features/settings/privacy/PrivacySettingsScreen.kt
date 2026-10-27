package com.nightcode.snout.features.settings.privacy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.compose.bottomOnly
import com.nightcode.snout.common.compose.withoutBottom
import com.nightcode.snout.designsystem.atoms.SectionLabel
import com.nightcode.snout.designsystem.atoms.SkeletonRows
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.molecules.InfoBanner
import com.nightcode.snout.designsystem.organisms.PushedScreenScaffold
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.shared.domain.model.telemetry.TelemetrySettings
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_open_in_new_24dp
import snout.composeapp.generated.resources.settings_privacy_errors_description
import snout.composeapp.generated.resources.settings_privacy_errors_title
import snout.composeapp.generated.resources.settings_privacy_note
import snout.composeapp.generated.resources.settings_privacy_policy_description
import snout.composeapp.generated.resources.settings_privacy_policy_title
import snout.composeapp.generated.resources.settings_privacy_reports_section
import snout.composeapp.generated.resources.settings_privacy_title
import snout.composeapp.generated.resources.settings_privacy_unavailable
import snout.composeapp.generated.resources.settings_privacy_usage_description
import snout.composeapp.generated.resources.settings_privacy_usage_title

private const val LoadingRowCount = 3

@Composable
internal fun PrivacySettingsScreen(
    state: PrivacySettingsUiState,
    onNavigateBack: () -> Unit,
    onUsageToggled: (Boolean) -> Unit,
    onErrorsToggled: (Boolean) -> Unit,
    onPrivacyPolicyClicked: () -> Unit,
) {
    PushedScreenScaffold(
        title = stringResource(Res.string.settings_privacy_title),
        onNavigateBack = onNavigateBack,
    ) { innerPadding ->
        val modifier = Modifier.fillMaxSize().padding(innerPadding.withoutBottom())
        val settings = state.settings
        if (settings == null) {
            SkeletonRows(modifier = modifier, count = LoadingRowCount)
        } else {
            PrivacySettingsContent(
                settings = settings,
                canReport = state.canReport,
                modifier = modifier,
                contentPadding = innerPadding.bottomOnly(),
                onUsageToggled = onUsageToggled,
                onErrorsToggled = onErrorsToggled,
                onPrivacyPolicyClicked = onPrivacyPolicyClicked,
            )
        }
    }
}

@Composable
private fun PrivacySettingsContent(
    settings: TelemetrySettings,
    canReport: Boolean,
    modifier: Modifier,
    contentPadding: PaddingValues,
    onUsageToggled: (Boolean) -> Unit,
    onErrorsToggled: (Boolean) -> Unit,
    onPrivacyPolicyClicked: () -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(bottom = Spacing.xxLarge),
    ) {
        // A build that never reports has nothing to switch: it says so, and keeps the policy.
        InfoBanner(
            text = stringResource(
                if (canReport) Res.string.settings_privacy_note else Res.string.settings_privacy_unavailable,
            ),
            modifier = Modifier.padding(horizontal = Spacing.large, vertical = Spacing.small),
        )

        if (canReport) {
            SectionLabel(stringResource(Res.string.settings_privacy_reports_section))
            SwitchRow(
                title = stringResource(Res.string.settings_privacy_usage_title),
                description = stringResource(Res.string.settings_privacy_usage_description),
                checked = settings.usage,
                divider = true,
                onCheckedChange = onUsageToggled,
            )
            SwitchRow(
                title = stringResource(Res.string.settings_privacy_errors_title),
                description = stringResource(Res.string.settings_privacy_errors_description),
                checked = settings.errors,
                divider = true,
                onCheckedChange = onErrorsToggled,
            )
        }
        EntityRow(
            title = stringResource(Res.string.settings_privacy_policy_title),
            subtitle = stringResource(Res.string.settings_privacy_policy_description),
            titleStyle = SnoutTheme.typography.bodyLarge,
            divider = false,
            onClick = onPrivacyPolicyClicked,
            trailing = {
                Icon(painter = painterResource(Res.drawable.ic_open_in_new_24dp), contentDescription = null)
            },
        )
    }
}

@Composable
private fun SwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    divider: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    EntityRow(
        title = title,
        subtitle = description,
        titleStyle = SnoutTheme.typography.bodyLarge,
        divider = divider,
        onClick = { onCheckedChange(checked.not()) },
        trailing = { Switch(checked = checked, onCheckedChange = onCheckedChange) },
    )
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun PrivacySettingsScreenPreview() = PrivacySettingsScreen(
    state = PrivacySettingsUiState(settings = TelemetrySettings(usage = false, errors = true)),
    onNavigateBack = {},
    onUsageToggled = {},
    onErrorsToggled = {},
    onPrivacyPolicyClicked = {},
)

/** A debug build, or one without Snout's PostHog key. */
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun PrivacySettingsScreenUnavailablePreview() = PrivacySettingsScreen(
    state = PrivacySettingsUiState(settings = TelemetrySettings.Default, canReport = false),
    onNavigateBack = {},
    onUsageToggled = {},
    onErrorsToggled = {},
    onPrivacyPolicyClicked = {},
)
