package com.nightcode.snout.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProject
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveThemeMode
import com.nightcode.snout.shared.domain.usecase.appstate.SaveThemeMode
import com.nightcode.snout.shared.domain.usecase.auth.GetInstanceUrl
import com.nightcode.snout.shared.domain.usecase.auth.Logout
import com.nightcode.snout.shared.domain.usecase.links.CanOpenLinkSettings
import com.nightcode.snout.shared.domain.usecase.links.OpenInBrowser
import com.nightcode.snout.shared.domain.usecase.links.OpenLinkSettings
import com.nightcode.snout.shared.domain.usecase.organization.ObserveOrganizations
import com.nightcode.snout.shared.domain.usecase.telemetry.TrackLoggedOut
import com.nightcode.snout.shared.domain.usecase.telemetry.TrackThemeChanged
import com.nightcode.snout.shared.domain.usecase.user.ObserveCurrentUser
import com.nightcode.snout.shared.domain.usecase.user.RefreshCurrentUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class SettingsViewModel(
    private val refreshCurrentUser: RefreshCurrentUser,
    private val getInstanceUrl: GetInstanceUrl,
    private val saveThemeMode: SaveThemeMode,
    private val logout: Logout,
    private val trackThemeChanged: TrackThemeChanged,
    private val trackLoggedOut: TrackLoggedOut,
    private val openInBrowser: OpenInBrowser,
    private val openLinkSettings: OpenLinkSettings,
    canOpenLinkSettings: CanOpenLinkSettings,
    observeCurrentUser: ObserveCurrentUser,
    observeOrganizations: ObserveOrganizations,
    observeSelectedProject: ObserveSelectedProject,
    observeThemeMode: ObserveThemeMode,
) : ViewModel() {

    /** Why the last account fetch failed; null while one runs or after it succeeded. */
    private val accountError = MutableStateFlow<DataError?>(null)
    private var accountLoad: Job? = null
    private var hasEntered = false
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

    /** The stored account shows at once; a fetch failure only shows while there is no stored account. */
    private val account = combine(observeCurrentUser(), accountError, ::Pair)

    val uiState: StateFlow<SettingsUiState> = combine(
        account,
        instanceUrl,
        selectedProjectNames,
        observeThemeMode(),
        isLoggingOut,
    ) { (account, accountError), instanceUrl, (organizationName, projectName), themeMode, isLoggingOut ->
        SettingsUiState(
            account = account,
            accountError = accountError,
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

    /** Refreshes the account, the only part of the screen fetched from PostHog. */
    fun refresh() {
        // A fetch finishing after the logout would store the account again.
        if (isLoggingOut.value) return
        accountLoad?.cancel()
        accountLoad = viewModelScope.launch {
            accountError.value = null
            accountError.value = refreshCurrentUser().leftOrNull()
        }
    }

    /** Refreshes the account each time the screen is entered again, but not the first time nor while it loads. */
    fun onScreenEntered() {
        if (hasEntered && accountLoad?.isActive != true) refresh()
        hasEntered = true
    }

    fun openInstance(url: String) = openInBrowser(url)

    fun openLinkSettings() = openLinkSettings.invoke()

    fun onThemeModeSelected(themeMode: ThemeMode) = viewModelScope.launch {
        saveThemeMode(themeMode)
        trackThemeChanged(themeMode)
    }

    fun onLogoutConfirmed() = viewModelScope.launch {
        isLoggingOut.value = true
        accountLoad?.cancel()
        trackLoggedOut()
        logout()
    }
}
