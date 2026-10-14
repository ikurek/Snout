package com.nightcode.snout.features.settings

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.user.TestUser
import com.nightcode.snout.shared.domain.model.user.User

internal data class SettingsUiState(
    /** The signed-in account, stored or just fetched; null until there is one. */
    val account: User? = null,
    /** Why the last account fetch failed; shown only while there is no [account]. */
    val accountError: DataError? = null,
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
    account = TestUser,
    instanceUrl = "https://eu.posthog.com",
    organizationName = "Nightcode",
    projectName = "My Project",
    themeMode = ThemeMode.System,
    canOpenLinkSettings = true,
)
