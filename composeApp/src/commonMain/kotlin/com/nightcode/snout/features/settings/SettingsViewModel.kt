package com.nightcode.snout.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.LoadState
import com.nightcode.snout.common.state.toLoadState
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode
import com.nightcode.snout.shared.domain.model.user.User
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProject
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveThemeMode
import com.nightcode.snout.shared.domain.usecase.appstate.SaveThemeMode
import com.nightcode.snout.shared.domain.usecase.auth.GetInstanceUrl
import com.nightcode.snout.shared.domain.usecase.auth.Logout
import com.nightcode.snout.shared.domain.usecase.links.CanOpenLinkSettings
import com.nightcode.snout.shared.domain.usecase.links.OpenInBrowser
import com.nightcode.snout.shared.domain.usecase.links.OpenLinkSettings
import com.nightcode.snout.shared.domain.usecase.organization.ObserveOrganizations
import com.nightcode.snout.shared.domain.usecase.user.GetCurrentUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class SettingsViewModel(
    private val getCurrentUser: GetCurrentUser,
    private val getInstanceUrl: GetInstanceUrl,
    private val saveThemeMode: SaveThemeMode,
    private val logout: Logout,
    private val openInBrowser: OpenInBrowser,
    private val openLinkSettings: OpenLinkSettings,
    canOpenLinkSettings: CanOpenLinkSettings,
    observeOrganizations: ObserveOrganizations,
    observeSelectedProject: ObserveSelectedProject,
    observeThemeMode: ObserveThemeMode,
) : ViewModel() {

    private val account = MutableStateFlow<LoadState<User>>(LoadState.Loading)
    private val instanceUrl = MutableStateFlow<String?>(null)
    private val isLoggingOut = MutableStateFlow(false)
    private val showLinkSettings = canOpenLinkSettings()

    private val selectedProjectNames = combine(
        observeOrganizations(),
        observeSelectedProject(),
    ) { organizations, selected ->
        val organization = organizations.firstOrNull { it.id == selected?.organizationId }
        val project = organization?.projects?.firstOrNull { it.id == selected?.projectId }
        organization?.name to project?.name
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        account,
        instanceUrl,
        selectedProjectNames,
        observeThemeMode(),
        isLoggingOut,
    ) { account, instanceUrl, (organizationName, projectName), themeMode, isLoggingOut ->
        SettingsUiState(
            account = account,
            instanceUrl = instanceUrl,
            organizationName = organizationName,
            projectName = projectName,
            themeMode = themeMode,
            isLoggingOut = isLoggingOut,
            canOpenLinkSettings = showLinkSettings,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SettingsUiState(canOpenLinkSettings = showLinkSettings),
    )

    init {
        refresh()
        viewModelScope.launch { instanceUrl.value = getInstanceUrl() }
    }

    /** Reloads the account, the only part of the screen fetched from PostHog. */
    fun refresh() = viewModelScope.launch {
        account.value = LoadState.Loading
        account.value = getCurrentUser().toLoadState()
    }

    fun openInstance(url: String) = openInBrowser(url)

    fun openLinkSettings() = openLinkSettings.invoke()

    fun onThemeModeSelected(themeMode: ThemeMode) = viewModelScope.launch { saveThemeMode(themeMode) }

    fun onLogoutConfirmed() = viewModelScope.launch {
        isLoggingOut.value = true
        logout()
    }
}
