package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences
import kotlinx.coroutines.flow.Flow

class ObserveNavigationPreferences(private val repository: AppStateRepository) {
    operator fun invoke(): Flow<NavigationPreferences> = repository.observeNavigationPreferences()
}
