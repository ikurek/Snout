package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.auth.AuthRepository

/** [Login] skips the intro for someone who connected before and only lost the key (a 401 or a revoked key). */
enum class InitialLocation { Intro, Login, ProjectSelection, Home }

class DetermineInitialLocation(
    private val authRepository: AuthRepository,
    private val appStateRepository: AppStateRepository,
) {
    suspend operator fun invoke(): InitialLocation = when {
        authRepository.getPersonalApiKey() == null -> when (authRepository.getInstanceUrl()) {
            null -> InitialLocation.Intro
            else -> InitialLocation.Login
        }

        appStateRepository.getSelectedProject() == null -> InitialLocation.ProjectSelection

        else -> InitialLocation.Home
    }
}
