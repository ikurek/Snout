package com.nightcode.snout.shared.domain.usecase.auth

import arrow.core.Either
import arrow.core.raise.either
import com.nightcode.snout.shared.domain.model.auth.PersonalApiKeyValidationError
class ValidatePersonalApiKey {
    suspend operator fun invoke(token: String): Either<PersonalApiKeyValidationError, Unit> = either {
        if (token.isBlank()) raise(PersonalApiKeyValidationError.ApiKeyIsEmpty)
        // TODO: Validate server-side
    }
}
