package com.nightcode.snout.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveNavigationPreferences
import com.nightcode.snout.shared.domain.usecase.links.FindLinkInText
import com.nightcode.snout.shared.domain.usecase.links.ParsePostHogLink
import com.nightcode.snout.shared.domain.usecase.links.SubmitIncomingLink
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class HomeViewModel(
    observeNavigationPreferences: ObserveNavigationPreferences,
    private val findLinkInText: FindLinkInText,
    private val parsePostHogLink: ParsePostHogLink,
    private val submitIncomingLink: SubmitIncomingLink,
) : ViewModel() {

    private val rejectedLink = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        observeNavigationPreferences(),
        rejectedLink,
    ) { navigationPreferences, rejectedLink ->
        HomeUiState(navigationPreferences = navigationPreferences, rejectedLink = rejectedLink)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    private val _sideEffects = Channel<HomeSideEffect>()
    val sideEffects: Flow<HomeSideEffect> = _sideEffects.receiveAsFlow()

    /**
     * Queues the PostHog link in [text], pasted into the open-link sheet. Only pages Snout can show: the sheet
     * isn't a way to open the browser, so anything else is rejected.
     */
    fun onLinkSubmitted(text: String) {
        val url = findLinkInText(text)?.takeIf { parsePostHogLink(it).isRight() }
        if (url == null) {
            rejectedLink.value = text
            return
        }
        rejectedLink.value = null
        viewModelScope.launch {
            // Close the sheet first: opening the link can push a screen over Home, which stops listening.
            _sideEffects.send(HomeSideEffect.LinkAccepted)
            submitIncomingLink(url)
        }
    }
}
