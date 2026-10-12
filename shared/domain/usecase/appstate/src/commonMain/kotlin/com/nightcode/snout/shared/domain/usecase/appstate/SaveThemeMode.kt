package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode
import org.koin.core.annotation.Single

@Single
class SaveThemeMode(private val repository: AppStateRepository) {
    suspend operator fun invoke(themeMode: ThemeMode) = repository.saveThemeMode(themeMode)
}
