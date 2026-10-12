package com.nightcode.snout.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode
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

    private val account = MutableStateFlow<AccountUiState>(AccountUiState.Loading)
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
        loadAccount()
        viewModelScope.launch { instanceUrl.value = getInstanceUrl() }
    }

    fun loadAccount() = viewModelScope.launch {
        account.value = AccountUiState.Loading
        account.value = getCurrentUser().fold(
            ifLeft = { AccountUiState.Error(it) },
            ifRight = { AccountUiState.Loaded(it) },
        )
    }

    fun onOpenInstance(url: String) = openInBrowser(url)

    fun onOpenLinkSettings() = openLinkSettings()

    fun onThemeModeSelected(themeMode: ThemeMode) = viewModelScope.launch { saveThemeMode(themeMode) }

    fun onLogoutConfirmed() = viewModelScope.launch {
        isLoggingOut.value = true
        logout()
    }
}
