package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode
import kotlinx.coroutines.flow.Flow

class ObserveThemeMode(private val repository: AppStateRepository) {
    operator fun invoke(): Flow<ThemeMode> = repository.observeThemeMode()
}
