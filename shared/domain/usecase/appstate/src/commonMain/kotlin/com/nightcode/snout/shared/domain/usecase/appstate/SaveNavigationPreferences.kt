package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences

class SaveNavigationPreferences(private val repository: AppStateRepository) {
    suspend operator fun invoke(preferences: NavigationPreferences) {
        require(preferences.isValid) { "Navigation preferences must hold four distinct tabs and land on one" }
        repository.saveNavigationPreferences(preferences)
    }
}
