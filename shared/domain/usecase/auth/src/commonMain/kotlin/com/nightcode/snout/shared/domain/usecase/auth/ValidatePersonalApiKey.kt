package com.nightcode.snout.shared.domain.usecase.auth

import arrow.core.Either
import arrow.core.raise.either
import com.nightcode.snout.shared.domain.model.auth.PersonalApiKeyValidationError

class ValidatePersonalApiKey {
    operator fun invoke(personalApiKey: String): Either<PersonalApiKeyValidationError, Unit> = either {
        if (personalApiKey.isBlank()) raise(PersonalApiKeyValidationError.ApiKeyIsEmpty)
    }
}
