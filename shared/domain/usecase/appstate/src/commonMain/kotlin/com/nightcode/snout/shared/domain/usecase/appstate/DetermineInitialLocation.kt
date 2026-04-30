package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.auth.AuthRepository

enum class InitialLocation { Login, ProjectSelection, Home }

class DetermineInitialLocation(
    private val authRepository: AuthRepository,
    private val appStateRepository: AppStateRepository,
) {
    suspend operator fun invoke(): InitialLocation = when {
        authRepository.getPersonalApiKey() == null -> InitialLocation.Login
        appStateRepository.getSelectedProject() == null -> InitialLocation.ProjectSelection
        else -> InitialLocation.Home
    }
}
