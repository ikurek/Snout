package com.nightcode.snout.widgets

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import com.nightcode.snout.shared.domain.model.auth.TestPersonalApiKey
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.organization.Organization
import com.nightcode.snout.shared.domain.model.organization.TestOrganization
import com.nightcode.snout.shared.domain.model.organization.TestProject
import com.nightcode.snout.shared.domain.usecase.appstate.IsSignedIn
import com.nightcode.snout.shared.domain.usecase.organization.ObserveOrganizations
import com.nightcode.snout.shared.domain.usecase.organization.RefreshOrganizations
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf

@OptIn(TestData::class)
class WidgetProjectsTest :
    ShouldSpec({
        val authRepository = mock<AuthRepository>()
        val repository = mock<OrganizationRepository>()
        val subject = WidgetProjects(
            isSignedIn = IsSignedIn(authRepository),
            refreshOrganizations = RefreshOrganizations(repository),
            observeOrganizations = ObserveOrganizations(repository),
        )

        fun stored(vararg organizations: Organization) {
            every { repository.observeOrganizations() } returns flowOf(organizations.toList())
        }

        beforeEach {
            everySuspend { authRepository.getPersonalApiKey() } returns TestPersonalApiKey
            everySuspend { repository.refreshOrganizations() } returns Either.Right(Unit)
        }

        should("offer every project, named with its organization, fetched from PostHog first") {
            stored(TestOrganization)
            subject.projects() shouldBe WidgetState.Loaded(
                listOf(ProjectChoice(TestProject.id, "My Project", "Nightcode")),
            )
            verifySuspend { repository.refreshOrganizations() }
        }

        should("fall back on the stored projects when PostHog can't be reached") {
            everySuspend { repository.refreshOrganizations() } returns Either.Left(DataError.ConnectionError)
            stored(TestOrganization)
            (subject.projects() as WidgetState.Loaded).content.map { it.id } shouldBe listOf(TestProject.id)
        }

        should("show the failure when there is nothing stored to fall back on") {
            everySuspend { repository.refreshOrganizations() } returns Either.Left(DataError.ConnectionError)
            stored()
            subject.projects() shouldBe WidgetState.Failed(DataError.ConnectionError)
        }
    })
