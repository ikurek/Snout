package com.nightcode.snout.shared.domain.usecase.user

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.user.UserRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.user.TestUser
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@OptIn(TestData::class)
class GetCurrentUserTest :
    ShouldSpec({
        val repository = mock<UserRepository>()
        val subject = GetCurrentUser(repository)

        should("return user on success") {
            everySuspend { repository.getCurrentUser() } returns Either.Right(TestUser)
            subject().shouldBeRight() shouldBe TestUser
        }

        should("return error on failure") {
            everySuspend { repository.getCurrentUser() } returns Either.Left(DataError.Unauthorized)
            subject().shouldBeLeft() shouldBe DataError.Unauthorized
        }
    })
