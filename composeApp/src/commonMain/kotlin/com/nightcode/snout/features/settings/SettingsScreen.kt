package com.nightcode.snout.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.compose.bottomOnly
import com.nightcode.snout.common.compose.withoutBottom
import com.nightcode.snout.common.format.formatMonthYear
import com.nightcode.snout.common.platform.appVersionName
import com.nightcode.snout.designsystem.actionFailedMessage
import com.nightcode.snout.designsystem.atoms.AvatarBox
import com.nightcode.snout.designsystem.atoms.SectionLabel
import com.nightcode.snout.designsystem.atoms.SkeletonBlock
import com.nightcode.snout.designsystem.atoms.VerticalSpacer
import com.nightcode.snout.designsystem.atoms.buttons.SecondaryButton
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.molecules.InlineErrorBanner
import com.nightcode.snout.designsystem.molecules.TonalCard
import com.nightcode.snout.designsystem.organisms.ConfirmationDialog
import com.nightcode.snout.designsystem.organisms.OptionPickerSheet
import com.nightcode.snout.designsystem.organisms.PickerOption
import com.nightcode.snout.designsystem.organisms.PushedScreenScaffold
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.features.settings.components.label
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.user.User
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_chevron_right_24dp
import snout.composeapp.generated.resources.ic_logout_24dp
import snout.composeapp.generated.resources.ic_open_in_new_24dp
import snout.composeapp.generated.resources.open_in_posthog_content_description
import snout.composeapp.generated.resources.settings_about_section
import snout.composeapp.generated.resources.settings_account_error
import snout.composeapp.generated.resources.settings_account_error_permission
import snout.composeapp.generated.resources.settings_account_error_rate_limited
import snout.composeapp.generated.resources.settings_account_member_since
import snout.composeapp.generated.resources.settings_analytics_description
import snout.composeapp.generated.resources.settings_analytics_title
import snout.composeapp.generated.resources.settings_connection_section
import snout.composeapp.generated.resources.settings_customise_section
import snout.composeapp.generated.resources.settings_instance_label
import snout.composeapp.generated.resources.settings_libraries_description
import snout.composeapp.generated.resources.settings_libraries_title
import snout.composeapp.generated.resources.settings_logout_button
import snout.composeapp.generated.resources.settings_logout_dialog_confirm
import snout.composeapp.generated.resources.settings_logout_dialog_text
import snout.composeapp.generated.resources.settings_logout_dialog_title
import snout.composeapp.generated.resources.settings_navigation_description
import snout.composeapp.generated.resources.settings_navigation_title
import snout.composeapp.generated.resources.settings_open_links_description
import snout.composeapp.generated.resources.settings_open_links_title
import snout.composeapp.generated.resources.settings_project_label
import snout.composeapp.generated.resources.settings_project_not_selected
import snout.composeapp.generated.resources.settings_screen_title
import snout.composeapp.generated.resources.settings_theme_title
import snout.composeapp.generated.resources.settings_version_footer

@Composable
internal fun SettingsScreen(
    state: SettingsUiState,
    onNavigateBack: () -> Unit,
    onRetryAccount: () -> Unit,
    onOpenInstance: (url: String) -> Unit,
    onSwitchProject: () -> Unit,
    onOpenLinkSettings: () -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onOpenNavigationSettings: () -> Unit,
    onOpenAnalyticsSettings: () -> Unit,
    onOpenLibraries: () -> Unit,
    onLogoutConfirmed: () -> Unit,
) {
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    PushedScreenScaffold(
        title = stringResource(Res.string.settings_screen_title),
        onNavigateBack = onNavigateBack,
    ) { innerPadding ->
        SettingsContent(
            state = state,
            modifier = Modifier.padding(innerPadding.withoutBottom()),
            contentPadding = innerPadding.bottomOnly(),
            onRetryAccount = onRetryAccount,
            onOpenInstance = onOpenInstance,
            onSwitchProject = onSwitchProject,
            onOpenLinkSettings = onOpenLinkSettings,
            onThemeModeSelected = onThemeModeSelected,
            onOpenNavigationSettings = onOpenNavigationSettings,
            onOpenAnalyticsSettings = onOpenAnalyticsSettings,
            onOpenLibraries = onOpenLibraries,
            onLogoutClicked = { showLogoutDialog = true },
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
    contentPadding: PaddingValues,
    onRetryAccount: () -> Unit,
    onOpenInstance: (url: String) -> Unit,
    onSwitchProject: () -> Unit,
    onOpenLinkSettings: () -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onOpenNavigationSettings: () -> Unit,
    onOpenAnalyticsSettings: () -> Unit,
    onOpenLibraries: () -> Unit,
    onLogoutClicked: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
    ) {
        AccountHeader(
            account = state.account,
            error = state.accountError,
            onRetry = onRetryAccount,
            modifier = Modifier.padding(horizontal = Spacing.large).padding(top = Spacing.small),
        )

        SectionLabel(stringResource(Res.string.settings_connection_section))
        ConnectionSection(state, onOpenInstance, onSwitchProject)

        SectionLabel(stringResource(Res.string.settings_customise_section))
        CustomiseSection(
            themeMode = state.themeMode,
            canOpenLinkSettings = state.canOpenLinkSettings,
            onThemeModeSelected = onThemeModeSelected,
            onOpenNavigationSettings = onOpenNavigationSettings,
            onOpenAnalyticsSettings = onOpenAnalyticsSettings,
            onOpenLinkSettings = onOpenLinkSettings,
        )

        SectionLabel(stringResource(Res.string.settings_about_section))
        SettingsRow(
            title = stringResource(Res.string.settings_libraries_title),
            supportingText = stringResource(Res.string.settings_libraries_description),
            divider = false,
            onClick = onOpenLibraries,
        )

        VerticalSpacer(Spacing.xxxLarge)
        SecondaryButton(
            text = stringResource(Res.string.settings_logout_button),
            onClick = onLogoutClicked,
            loading = state.isLoggingOut,
            destructive = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.large),
            icon = painterResource(Res.drawable.ic_logout_24dp),
        )
        VerticalSpacer(Spacing.large)
        Text(
            text = stringResource(Res.string.settings_version_footer, appVersionName()),
            style = SnoutTheme.typography.bodySmall,
            color = SnoutTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.large),
        )
        VerticalSpacer(Spacing.xxLarge)
    }
}

/** Who is signed in, on a tonal card at the top: the one place on the screen that is fetched from PostHog. */
@Composable
private fun AccountHeader(account: User?, error: DataError?, onRetry: () -> Unit, modifier: Modifier) {
    when {
        account != null -> TonalCard(modifier = modifier, contentPadding = PaddingValues(Spacing.large)) {
            AccountHeaderContent(account)
        }

        error != null -> InlineErrorBanner(
            text = stringResource(
                error.actionFailedMessage(
                    generic = Res.string.settings_account_error,
                    forbidden = Res.string.settings_account_error_permission,
                    rateLimited = Res.string.settings_account_error_rate_limited,
                ),
            ),
            onAction = onRetry,
            modifier = modifier,
        )

        else -> TonalCard(modifier = modifier, contentPadding = PaddingValues(Spacing.large)) {
            AccountHeaderSkeleton()
        }
    }
}

@Composable
private fun AccountHeaderContent(user: User) {
    val name = user.fullName.ifBlank { user.email }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.large),
    ) {
        AvatarBox(name = name, size = AccountAvatarSize)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = SnoutTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = user.email,
                style = SnoutTheme.typography.bodyMedium,
                color = SnoutTheme.colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(Res.string.settings_account_member_since, user.dateJoined.formatMonthYear()),
                style = SnoutTheme.typography.bodySmall,
                color = SnoutTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.xxSmall),
            )
        }
    }
}

@Composable
private fun AccountHeaderSkeleton() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.large),
    ) {
        SkeletonBlock(Modifier.size(AccountAvatarSize))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            SkeletonBlock(Modifier.fillMaxWidth(SkeletonNameWidth).height(16.dp))
            SkeletonBlock(Modifier.fillMaxWidth(SkeletonEmailWidth).height(12.dp))
            SkeletonBlock(Modifier.fillMaxWidth(SkeletonMetaWidth).height(10.dp))
        }
    }
}

@Composable
private fun ConnectionSection(
    state: SettingsUiState,
    onOpenInstance: (url: String) -> Unit,
    onSwitchProject: () -> Unit,
) {
    val projectValue = when {
        state.projectName == null -> stringResource(Res.string.settings_project_not_selected)
        state.organizationName == null -> state.projectName
        else -> "${state.organizationName} · ${state.projectName}"
    }
    val instanceUrl = state.instanceUrl
    SettingsRow(
        title = stringResource(Res.string.settings_instance_label),
        supportingText = instanceUrl,
        onClick = instanceUrl?.let { url -> { onOpenInstance(url) } },
        trailing = {
            if (instanceUrl != null) {
                Icon(
                    painter = painterResource(Res.drawable.ic_open_in_new_24dp),
                    contentDescription = stringResource(Res.string.open_in_posthog_content_description),
                    tint = SnoutTheme.colors.primary,
                )
            }
        },
    )
    SettingsRow(
        title = stringResource(Res.string.settings_project_label),
        supportingText = projectValue,
        divider = false,
        onClick = onSwitchProject,
    )
}

/**
 * How the app looks and behaves on this device: the theme (picked in a sheet), the sub-screens that shape the
 * bottom bar and the Analytics tab, and, where the platform has it, the system setting that sends PostHog links to
 * Snout.
 */
@Composable
private fun CustomiseSection(
    themeMode: ThemeMode,
    canOpenLinkSettings: Boolean,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onOpenNavigationSettings: () -> Unit,
    onOpenAnalyticsSettings: () -> Unit,
    onOpenLinkSettings: () -> Unit,
) {
    var showThemePicker by rememberSaveable { mutableStateOf(false) }
    val themeTitle = stringResource(Res.string.settings_theme_title)
    SettingsRow(
        title = themeTitle,
        supportingText = stringResource(themeMode.label),
        onClick = { showThemePicker = true },
    )
    OptionPickerSheet(
        isVisible = showThemePicker,
        title = themeTitle,
        options = ThemeMode.entries.map { PickerOption(value = it, title = stringResource(it.label)) },
        selected = themeMode,
        onSelect = onThemeModeSelected,
        onDismiss = { showThemePicker = false },
    )
    SettingsRow(
        title = stringResource(Res.string.settings_navigation_title),
        supportingText = stringResource(Res.string.settings_navigation_description),
        onClick = onOpenNavigationSettings,
    )
    SettingsRow(
        title = stringResource(Res.string.settings_analytics_title),
        supportingText = stringResource(Res.string.settings_analytics_description),
        divider = canOpenLinkSettings,
        onClick = onOpenAnalyticsSettings,
    )
    if (canOpenLinkSettings) {
        SettingsRow(
            title = stringResource(Res.string.settings_open_links_title),
            supportingText = stringResource(Res.string.settings_open_links_description),
            divider = false,
            onClick = onOpenLinkSettings,
        )
    }
}

/**
 * The one row shape on this screen: a `bodyLarge` title, optional supporting text and a trailing chevron unless
 * [trailing] puts another action icon there. Rows within a section are split by an inset hairline; the last row of
 * a section passes `divider = false`.
 */
@Composable
private fun SettingsRow(
    title: String,
    supportingText: String?,
    onClick: (() -> Unit)?,
    divider: Boolean = true,
    trailing: @Composable () -> Unit = { Chevron() },
) {
    EntityRow(
        title = title,
        subtitle = supportingText,
        titleStyle = SettingsRowTitleStyle,
        divider = divider,
        onClick = onClick,
        trailing = trailing,
    )
}

private val SettingsRowTitleStyle: TextStyle
    @Composable get() = SnoutTheme.typography.bodyLarge

private val AccountAvatarSize = 40.dp
private const val SkeletonNameWidth = 0.45f
private const val SkeletonEmailWidth = 0.65f
private const val SkeletonMetaWidth = 0.4f

@Composable
private fun Chevron() {
    Icon(
        painter = painterResource(Res.drawable.ic_chevron_right_24dp),
        contentDescription = null,
        tint = SnoutTheme.colors.onSurfaceVariant,
    )
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
    onSwitchProject = {},
    onOpenLinkSettings = {},
    onThemeModeSelected = {},
    onOpenNavigationSettings = {},
    onOpenAnalyticsSettings = {},
    onOpenLibraries = {},
    onLogoutConfirmed = {},
)

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun SettingsScreenAccountErrorPreview() = SettingsScreen(
    state = TestSettingsUiState.copy(account = null, accountError = DataError.ConnectionError),
    onNavigateBack = {},
    onRetryAccount = {},
    onOpenInstance = {},
    onSwitchProject = {},
    onOpenLinkSettings = {},
    onThemeModeSelected = {},
    onOpenNavigationSettings = {},
    onOpenAnalyticsSettings = {},
    onOpenLibraries = {},
    onLogoutConfirmed = {},
)
