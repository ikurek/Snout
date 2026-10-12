package com.nightcode.snout.features.settings

import com.nightcode.snout.common.state.LoadState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode
import com.nightcode.snout.shared.domain.model.user.TestUser
import com.nightcode.snout.shared.domain.model.user.User

internal data class SettingsUiState(
    val account: LoadState<User> = LoadState.Loading,
    val instanceUrl: String? = null,
    val organizationName: String? = null,
    val projectName: String? = null,
    val themeMode: ThemeMode = ThemeMode.System,
    val isLoggingOut: Boolean = false,
    val canOpenLinkSettings: Boolean = false,
)

@TestData
@OptIn(TestData::class)
internal val TestSettingsUiState = SettingsUiState(
    account = LoadState.Loaded(TestUser),
    instanceUrl = "https://eu.posthog.com",
    organizationName = "Nightcode",
    projectName = "My Project",
    themeMode = ThemeMode.System,
    canOpenLinkSettings = true,
)
