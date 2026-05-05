package com.nightcode.snout.shared.domain.usecase.auth

import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

class ShouldReturnToLoginTest :
    ShouldSpec({
        val repository = mock<AuthRepository>(autoUnit)
        val subject = ShouldReturnToLogin(repository)

        should("emit unit when api key is null") {
            every { repository.observePersonalApiKey() } returns flowOf(null)
            subject().first() shouldBe Unit
        }
    })
