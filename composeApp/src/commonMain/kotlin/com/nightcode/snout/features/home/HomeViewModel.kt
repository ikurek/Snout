package com.nightcode.snout.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveNavigationPreferences
import com.nightcode.snout.shared.domain.usecase.links.FindLinkInText
import com.nightcode.snout.shared.domain.usecase.links.SubmitIncomingLink
import com.nightcode.snout.shared.domain.usecase.links.parsePostHogLink
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class HomeViewModel(
    @InjectedParam initialNavigationPreferences: NavigationPreferences,
    observeNavigationPreferences: ObserveNavigationPreferences,
    private val findLinkInText: FindLinkInText,
    private val submitIncomingLink: SubmitIncomingLink,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = observeNavigationPreferences()
        .map { HomeUiState(navigationPreferences = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(navigationPreferences = initialNavigationPreferences),
        )

    /**
     * Queues the PostHog link in [text], pasted into the open-link sheet, and returns whether it was accepted.
     * Only pages Snout can show: the sheet isn't a way to open the browser, so anything else is rejected.
     */
    fun onLinkSubmitted(text: String): Boolean {
        val url = findLinkInText(text)?.takeIf { parsePostHogLink(it) != null } ?: return false
        viewModelScope.launch { submitIncomingLink(url) }
        return true
    }
}
