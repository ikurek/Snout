package com.nightcode.snout.features.settings.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.model.appstate.HomeSection
import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveNavigationPreferences
import com.nightcode.snout.shared.domain.usecase.appstate.SaveNavigationPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class NavigationSettingsViewModel(
    private val saveNavigationPreferences: SaveNavigationPreferences,
    observeNavigationPreferences: ObserveNavigationPreferences,
) : ViewModel() {

    val uiState: StateFlow<NavigationPreferences?> = observeNavigationPreferences()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Puts [section] in [slot]; a section already in the bar swaps places with the one it replaces. */
    fun onSlotSelected(slot: Int, section: HomeSection) {
        val current = uiState.value ?: return
        viewModelScope.launch { saveNavigationPreferences(current.withTab(slot, section)) }
    }

    fun onLandingSelected(section: HomeSection) {
        val current = uiState.value ?: return
        viewModelScope.launch { saveNavigationPreferences(current.withLanding(section)) }
    }

    fun onReset() = viewModelScope.launch { saveNavigationPreferences(NavigationPreferences.Default) }
}
