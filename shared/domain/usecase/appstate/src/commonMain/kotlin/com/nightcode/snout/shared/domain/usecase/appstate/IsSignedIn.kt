package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import org.koin.core.annotation.Single

/** Whether a key is stored: what the home-screen widgets check before loading, to ask for signing in instead. */
@Single
class IsSignedIn(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): Boolean = authRepository.getPersonalApiKey() != null
}
