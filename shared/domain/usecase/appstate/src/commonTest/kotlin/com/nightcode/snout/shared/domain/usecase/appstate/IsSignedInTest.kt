package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.model.auth.TestPersonalApiKey
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@OptIn(TestData::class)
class IsSignedInTest :
    ShouldSpec({
        val authRepository = mock<AuthRepository>()
        val subject = IsSignedIn(authRepository)

        should("be signed in with a key stored") {
            everySuspend { authRepository.getPersonalApiKey() } returns TestPersonalApiKey
            subject() shouldBe true
        }

        should("be signed out without one, whatever else is saved") {
            everySuspend { authRepository.getPersonalApiKey() } returns null
            subject() shouldBe false
        }
    })
