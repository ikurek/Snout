package com.nightcode.snout.shared.domain.usecase.organization

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec

class RefreshOrganizationsTest :
    ShouldSpec({
        val repository = mock<OrganizationRepository>()
        val subject = RefreshOrganizations(repository)

        should("return right on success") {
            everySuspend { repository.refreshOrganizations() } returns Either.Right(Unit)
            subject().shouldBeRight()
        }

        should("return left on failure") {
            everySuspend { repository.refreshOrganizations() } returns Either.Left(
                DataError.ConnectionError,
            )
            subject().shouldBeLeft(DataError.ConnectionError)
        }
    })
