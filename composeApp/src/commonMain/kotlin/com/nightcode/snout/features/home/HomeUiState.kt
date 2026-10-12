package com.nightcode.snout.features.home

import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences

internal data class HomeUiState(
    /** The bar and the landing tab; null until read, and Home draws nothing rather than flash the defaults. */
    val navigationPreferences: NavigationPreferences? = null,
    /** Text the open-link sheet couldn't open; its error shows while the field still holds exactly this. */
    val rejectedLink: String? = null,
)

internal sealed interface HomeSideEffect {
    /** The open-link sheet's link was queued; the sheet closes and forgets it. */
    data object LinkAccepted : HomeSideEffect
}
