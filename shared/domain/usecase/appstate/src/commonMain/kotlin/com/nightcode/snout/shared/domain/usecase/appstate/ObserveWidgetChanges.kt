package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

/**
 * Emits each time the key changes (signing in or out), the one thing every home-screen widget depends on: each
 * names its own project, whichever the app has selected. Not for the current key, only for later changes, so the
 * platform redraws its widgets at once instead of at their next refresh.
 */
@Single
class ObserveWidgetChanges(private val authRepository: AuthRepository) {
    operator fun invoke(): Flow<Unit> = authRepository.observePersonalApiKey()
        .distinctUntilChanged()
        .drop(1)
        .map { }
}
