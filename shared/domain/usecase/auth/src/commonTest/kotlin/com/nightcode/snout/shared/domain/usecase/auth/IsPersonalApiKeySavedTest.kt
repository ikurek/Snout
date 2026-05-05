package com.nightcode.snout.shared.domain.usecase.auth

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.model.auth.TestPersonalApiKey
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@OptIn(TestData::class)
class IsPersonalApiKeySavedTest :
    ShouldSpec({
        val repository = mock<AuthRepository>(autoUnit)
        val subject = IsPersonalApiKeySaved(repository)

        should("return false when no api key is saved") {
            everySuspend { repository.getPersonalApiKey() } returns null
            subject() shouldBe false
        }

        should("return true when api key is saved") {
            everySuspend { repository.getPersonalApiKey() } returns TestPersonalApiKey
            subject() shouldBe true
        }
    })
