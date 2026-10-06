package com.nightcode.snout.features.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.format.formatMonthYear
import com.nightcode.snout.common.platform.appVersionName
import com.nightcode.snout.designsystem.atoms.AvatarBox
import com.nightcode.snout.designsystem.atoms.SectionLabel
import com.nightcode.snout.designsystem.atoms.SkeletonRow
import com.nightcode.snout.designsystem.atoms.VerticalSpacer
import com.nightcode.snout.designsystem.atoms.buttons.NavigationBackIconButton
import com.nightcode.snout.designsystem.atoms.buttons.SecondaryButton
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.molecules.InlineErrorBanner
import com.nightcode.snout.designsystem.molecules.KeyValueRow
import com.nightcode.snout.designsystem.molecules.MetaText
import com.nightcode.snout.designsystem.molecules.SegmentedControl
import com.nightcode.snout.designsystem.organisms.ConfirmationDialog
import com.nightcode.snout.designsystem.organisms.SnoutTopAppBar
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode
import com.nightcode.snout.shared.domain.model.user.GetCurrentUserError
import com.nightcode.snout.shared.domain.model.user.User
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_logout_24dp
import snout.composeapp.generated.resources.ic_open_in_new_24dp
import snout.composeapp.generated.resources.open_in_posthog_content_description
import snout.composeapp.generated.resources.settings_about_section
import snout.composeapp.generated.resources.settings_account_error
import snout.composeapp.generated.resources.settings_account_error_permission
import snout.composeapp.generated.resources.settings_account_error_rate_limited
import snout.composeapp.generated.resources.settings_account_member_since
import snout.composeapp.generated.resources.settings_account_section
import snout.composeapp.generated.resources.settings_appearance_section
import snout.composeapp.generated.resources.settings_connection_section
import snout.composeapp.generated.resources.settings_instance_label
import snout.composeapp.generated.resources.settings_logout_button
import snout.composeapp.generated.resources.settings_logout_dialog_confirm
import snout.composeapp.generated.resources.settings_logout_dialog_text
import snout.composeapp.generated.resources.settings_logout_dialog_title
import snout.composeapp.generated.resources.settings_project_label
import snout.composeapp.generated.resources.settings_project_not_selected
import snout.composeapp.generated.resources.settings_screen_title
import snout.composeapp.generated.resources.settings_theme_dark
import snout.composeapp.generated.resources.settings_theme_light
import snout.composeapp.generated.resources.settings_theme_system
import snout.composeapp.generated.resources.settings_version_label

@Composable
internal fun SettingsScreen(
    state: SettingsUiState,
    onNavigateBack: () -> Unit,
    onRetryAccount: () -> Unit,
    onOpenInstance: (url: String) -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onLogoutConfirmed: () -> Unit,
) {
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SnoutTopAppBar(
                title = stringResource(Res.string.settings_screen_title),
                navigationIcon = { NavigationBackIconButton(onClick = onNavigateBack) },
            )
        },
    ) { innerPadding ->
        SettingsContent(
            state = state,
            modifier = Modifier.padding(innerPadding),
            onRetryAccount = onRetryAccount,
            onOpenInstance = onOpenInstance,
            onThemeModeSelected = onThemeModeSelected,
            onLogoutClick = { showLogoutDialog = true },
        )
    }

    if (showLogoutDialog) {
        ConfirmationDialog(
            title = stringResource(Res.string.settings_logout_dialog_title),
            text = stringResource(Res.string.settings_logout_dialog_text),
            confirmLabel = stringResource(Res.string.settings_logout_dialog_confirm),
            destructive = true,
            onConfirm = {
                showLogoutDialog = false
                onLogoutConfirmed()
            },
            onDismiss = { showLogoutDialog = false },
        )
    }
}

@Composable
private fun SettingsContent(
    state: SettingsUiState,
    modifier: Modifier,
    onRetryAccount: () -> Unit,
    onOpenInstance: (url: String) -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onLogoutClick: () -> Unit,
) {
    val themeLabels = ThemeMode.entries.associateWith { it.label() }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        SectionLabel(stringResource(Res.string.settings_account_section))
        AccountSection(state.account, onRetry = onRetryAccount)

        SectionLabel(stringResource(Res.string.settings_connection_section))
        ConnectionSection(state, onOpenInstance)

        SectionLabel(stringResource(Res.string.settings_appearance_section))
        SegmentedControl(
            options = ThemeMode.entries,
            selected = state.themeMode,
            onSelect = onThemeModeSelected,
            label = { themeLabels.getValue(it) },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )

        SectionLabel(stringResource(Res.string.settings_about_section))
        KeyValueRow(label = stringResource(Res.string.settings_version_label), value = appVersionName())

        VerticalSpacer(24.dp)
        HorizontalDivider(color = SnoutTheme.colors.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
        VerticalSpacer(24.dp)
        SecondaryButton(
            text = stringResource(Res.string.settings_logout_button),
            onClick = onLogoutClick,
            loading = state.isLoggingOut,
            destructive = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            icon = painterResource(Res.drawable.ic_logout_24dp),
        )
        VerticalSpacer(32.dp)
    }
}

@Composable
private fun AccountSection(account: AccountUiState, onRetry: () -> Unit) {
    when (account) {
        AccountUiState.Loading -> SkeletonRow(divider = false)

        is AccountUiState.Error -> InlineErrorBanner(
            text = stringResource(account.error.message()),
            onAction = onRetry,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )

        is AccountUiState.Loaded -> AccountRow(account.user)
    }
}

@Composable
private fun AccountRow(user: User) {
    val name = user.fullName.ifBlank { user.email }
    EntityRow(
        title = name,
        subtitle = user.email,
        subtitleStyle = SnoutTheme.typography.bodySmall,
        divider = false,
        leading = { AvatarBox(name = name) },
        supporting = {
            MetaText(stringResource(Res.string.settings_account_member_since, user.dateJoined.formatMonthYear()))
        },
    )
}

@Composable
private fun ConnectionSection(state: SettingsUiState, onOpenInstance: (url: String) -> Unit) {
    val projectValue = when {
        state.projectName == null -> stringResource(Res.string.settings_project_not_selected)
        state.organizationName == null -> state.projectName
        else -> "${state.organizationName} · ${state.projectName}"
    }
    KeyValueRow(
        label = stringResource(Res.string.settings_instance_label),
        value = state.instanceUrl.orEmpty(),
        valueMaxLines = 1,
        trailing = state.instanceUrl?.let { url ->
            {
                IconButton(onClick = { onOpenInstance(url) }) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_open_in_new_24dp),
                        contentDescription = stringResource(Res.string.open_in_posthog_content_description),
                        tint = SnoutTheme.colors.primary,
                    )
                }
            }
        },
    )
    KeyValueRow(label = stringResource(Res.string.settings_project_label), value = projectValue)
}

@Composable
private fun ThemeMode.label(): String = stringResource(
    when (this) {
        ThemeMode.System -> Res.string.settings_theme_system
        ThemeMode.Light -> Res.string.settings_theme_light
        ThemeMode.Dark -> Res.string.settings_theme_dark
    },
)

private fun GetCurrentUserError.message() = when (this) {
    GetCurrentUserError.Unauthorized, GetCurrentUserError.Forbidden -> Res.string.settings_account_error_permission
    GetCurrentUserError.RateLimited -> Res.string.settings_account_error_rate_limited
    GetCurrentUserError.ConnectionError, GetCurrentUserError.UnexpectedError -> Res.string.settings_account_error
}

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun SettingsScreenPreview() = SettingsScreen(
    state = TestSettingsUiState,
    onNavigateBack = {},
    onRetryAccount = {},
    onOpenInstance = {},
    onThemeModeSelected = {},
    onLogoutConfirmed = {},
)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun SettingsScreenAccountErrorPreview() = SettingsScreen(
    state = TestSettingsUiState.copy(account = AccountUiState.Error(GetCurrentUserError.ConnectionError)),
    onNavigateBack = {},
    onRetryAccount = {},
    onOpenInstance = {},
    onThemeModeSelected = {},
    onLogoutConfirmed = {},
)
