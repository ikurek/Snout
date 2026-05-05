package com.nightcode.snout.shared.domain.usecase.auth

import com.nightcode.snout.shared.domain.model.auth.PersonalApiKeyValidationError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec

class ValidatePersonalApiKeyTest :
    ShouldSpec({
        val subject = ValidatePersonalApiKey()

        should("return right for non-blank api key") {
            subject("phx_abc123").shouldBeRight()
        }

        should("return ApiKeyIsEmpty when api key is blank") {
            subject("").shouldBeLeft(PersonalApiKeyValidationError.ApiKeyIsEmpty)
        }

        should("return ApiKeyIsEmpty when api key is whitespace only") {
            subject("   ").shouldBeLeft(PersonalApiKeyValidationError.ApiKeyIsEmpty)
        }
    })
