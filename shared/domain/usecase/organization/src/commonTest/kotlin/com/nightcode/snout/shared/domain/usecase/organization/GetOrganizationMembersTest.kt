package com.nightcode.snout.shared.domain.usecase.organization

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.organization.TestOrganizationMembers
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProject
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@OptIn(TestData::class)
class GetOrganizationMembersTest :
    ShouldSpec({
        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<OrganizationRepository>()
        val subject = GetOrganizationMembers(GetSelectedProject(appStateRepository), repository)

        should("get the members of the selected project's organization") {
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            everySuspend { repository.getMembers(TestSelectedProject.organizationId) } returns
                Either.Right(TestOrganizationMembers)

            subject() shouldBe Either.Right(TestOrganizationMembers)
        }

        should("fail when no project is selected") {
            everySuspend { appStateRepository.getSelectedProject() } returns null

            shouldThrow<IllegalStateException> { subject() }
        }
    })
