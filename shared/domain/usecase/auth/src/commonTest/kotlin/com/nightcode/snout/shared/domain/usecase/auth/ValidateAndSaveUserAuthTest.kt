package com.nightcode.snout.shared.domain.usecase.auth

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.data.user.UserRepository
import com.nightcode.snout.shared.domain.model.auth.PersonalApiKey
import com.nightcode.snout.shared.domain.model.user.GetCurrentUserError
import com.nightcode.snout.shared.domain.model.user.TestUser
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@OptIn(TestData::class)
class ValidateAndSaveUserAuthTest :
    ShouldSpec({
        val authRepository = mock<AuthRepository>(autoUnit)
        val userRepository = mock<UserRepository>()
        val subject = ValidateAndSaveUserAuth(authRepository, userRepository)

        should("return user on success and save api key") {
            everySuspend { userRepository.getCurrentUser() } returns Either.Right(TestUser)
            val result = subject("phx_token", "https://app.posthog.com")
            result.shouldBeRight() shouldBe TestUser
            verifySuspend { authRepository.savePersonalApiKey(PersonalApiKey("phx_token")) }
        }

        should("clear api key on failure and return error") {
            everySuspend { userRepository.getCurrentUser() } returns Either.Left(GetCurrentUserError.Unauthorized)
            val result = subject("phx_token", "https://app.posthog.com")
            result.shouldBeLeft() shouldBe GetCurrentUserError.Unauthorized
            verifySuspend { authRepository.clear() }
        }
    })
