package com.nightcode.snout.features.home

import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences

internal data class HomeUiState(
    /** The bar and the landing tab. */
    val navigationPreferences: NavigationPreferences,
)
