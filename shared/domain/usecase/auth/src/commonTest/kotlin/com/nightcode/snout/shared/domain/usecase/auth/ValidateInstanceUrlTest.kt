package com.nightcode.snout.shared.domain.usecase.auth

import com.nightcode.snout.shared.domain.model.auth.InstanceUrlValidationError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec

class ValidateInstanceUrlTest :
    ShouldSpec({
        val subject = ValidateInstanceUrl()

        should("return right for valid https url") {
            subject("https://app.posthog.com").shouldBeRight()
        }

        should("return UrlIsEmpty when url is blank") {
            subject("").shouldBeLeft(InstanceUrlValidationError.UrlIsEmpty)
        }

        should("return UrlIsInvalid when url does not start with https") {
            subject("http://app.posthog.com").shouldBeLeft(InstanceUrlValidationError.UrlIsInvalid)
        }

        should("return UrlIsInvalid when url is not https") {
            subject("ftp://app.posthog.com").shouldBeLeft(InstanceUrlValidationError.UrlIsInvalid)
        }
    })
