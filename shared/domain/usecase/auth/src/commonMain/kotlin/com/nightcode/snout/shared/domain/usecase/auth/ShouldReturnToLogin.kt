package com.nightcode.snout.shared.domain.usecase.auth

import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class ShouldReturnToLogin(private val repository: AuthRepository) {
    operator fun invoke(): Flow<Unit> = repository.observePersonalApiKey()
        .filter { it == null }
        .map { }
}
