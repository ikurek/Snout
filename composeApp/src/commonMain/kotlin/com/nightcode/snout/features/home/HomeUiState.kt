package com.nightcode.snout.features.home

import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences

internal data class HomeUiState(
    /** The bar and the landing tab; null until read, and Home draws nothing rather than flash the defaults. */
    val navigationPreferences: NavigationPreferences? = null,
)
