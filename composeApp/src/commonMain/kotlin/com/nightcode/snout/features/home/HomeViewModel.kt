package com.nightcode.snout.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveNavigationPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class HomeViewModel(
    @InjectedParam initialNavigationPreferences: NavigationPreferences,
    observeNavigationPreferences: ObserveNavigationPreferences,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = observeNavigationPreferences()
        .map { HomeUiState(navigationPreferences = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(navigationPreferences = initialNavigationPreferences),
        )
}
