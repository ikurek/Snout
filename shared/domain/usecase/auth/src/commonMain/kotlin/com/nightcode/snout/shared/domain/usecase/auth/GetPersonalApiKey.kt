package com.nightcode.snout.shared.domain.usecase.auth

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.model.auth.PersonalApiKey
class GetPersonalApiKey(private val repository: AuthRepository) {
    suspend operator fun invoke(): Either<Unit, PersonalApiKey> = either {
        val token = repository.getPersonalApiKey()
        ensureNotNull(token) {}
    }
}
