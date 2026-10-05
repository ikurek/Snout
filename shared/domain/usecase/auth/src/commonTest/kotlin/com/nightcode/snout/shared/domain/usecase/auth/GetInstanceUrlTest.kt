package com.nightcode.snout.shared.domain.usecase.auth

import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class GetInstanceUrlTest :
    ShouldSpec({
        val repository = mock<AuthRepository>()
        val subject = GetInstanceUrl(repository)

        should("return repository instance url") {
            everySuspend { repository.getInstanceUrl() } returns "https://eu.posthog.com"
            subject() shouldBe "https://eu.posthog.com"
        }
    })
