package com.nightcode.snout.shared.domain.usecase.auth

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.data.user.UserRepository
import com.nightcode.snout.shared.domain.model.auth.PersonalApiKey
import com.nightcode.snout.shared.domain.model.common.DataError
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exhaustiveOrder
import dev.mokkery.verifySuspend
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class ValidateAndSaveUserAuthTest :
    ShouldSpec({
        val authRepository = mock<AuthRepository>(autoUnit)
        val userRepository = mock<UserRepository>()
        val subject = ValidateAndSaveUserAuth(authRepository, userRepository)

        should("save the connection and store the account on success") {
            everySuspend { userRepository.refreshCurrentUser() } returns Either.Right(Unit)
            subject("phx_token", "https://app.posthog.com").shouldBeRight()
            verifySuspend(exhaustiveOrder) {
                authRepository.savePersonalApiKey(PersonalApiKey("phx_token"))
                authRepository.saveInstanceUrl("https://app.posthog.com")
                userRepository.refreshCurrentUser()
            }
        }

        should("clear api key on failure and return error") {
            everySuspend { userRepository.refreshCurrentUser() } returns Either.Left(DataError.Unauthorized)
            val result = subject("phx_token", "https://app.posthog.com")
            result.shouldBeLeft() shouldBe DataError.Unauthorized
            verifySuspend { authRepository.clear() }
        }
    })
