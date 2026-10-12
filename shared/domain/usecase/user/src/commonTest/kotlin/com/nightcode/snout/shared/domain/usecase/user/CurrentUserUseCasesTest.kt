package com.nightcode.snout.shared.domain.usecase.user

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.user.UserRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.user.TestUser
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

@OptIn(TestData::class)
class CurrentUserUseCasesTest :
    ShouldSpec({
        val repository = mock<UserRepository>()

        should("observe the stored account") {
            every { repository.observeCurrentUser() } returns flowOf(TestUser)
            ObserveCurrentUser(repository)().first() shouldBe TestUser
        }

        should("refresh the account") {
            everySuspend { repository.refreshCurrentUser() } returns Either.Right(Unit)
            RefreshCurrentUser(repository)().shouldBeRight()
        }

        should("return the refresh error") {
            everySuspend { repository.refreshCurrentUser() } returns Either.Left(DataError.Unauthorized)
            RefreshCurrentUser(repository)().shouldBeLeft() shouldBe DataError.Unauthorized
        }
    })
