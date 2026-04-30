package com.nightcode.snout.shared.domain.usecase.auth

import arrow.core.Either
import arrow.core.raise.either
import com.nightcode.snout.shared.domain.model.auth.InstanceUrlValidationError

class ValidateInstanceUrl {
    operator fun invoke(instanceUrl: String): Either<InstanceUrlValidationError, Unit> = either {
        if (instanceUrl.isBlank()) raise(InstanceUrlValidationError.UrlIsEmpty)
        if (instanceUrl.startsWith("https://").not()) raise(InstanceUrlValidationError.UrlIsInvalid)
    }
}
