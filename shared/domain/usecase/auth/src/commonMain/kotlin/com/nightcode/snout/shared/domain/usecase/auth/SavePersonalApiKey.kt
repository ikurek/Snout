package com.nightcode.snout.shared.domain.usecase.auth

import arrow.core.Either
import arrow.core.raise.either
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.model.auth.PersonalApiKey
class SavePersonalApiKey(private val repository: AuthRepository) {
    suspend operator fun invoke(token: String): Either<Unit, Unit> = either {
        if (token.isBlank()) raise(Unit)

        repository.savePersonalApiKey(PersonalApiKey(token))
    }
}
