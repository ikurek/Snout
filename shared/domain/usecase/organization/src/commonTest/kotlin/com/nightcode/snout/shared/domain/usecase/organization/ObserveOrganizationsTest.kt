package com.nightcode.snout.shared.domain.usecase.organization

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import com.nightcode.snout.shared.domain.model.organization.TestOrganization
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

@OptIn(TestData::class)
class ObserveOrganizationsTest :
    ShouldSpec({
        val repository = mock<OrganizationRepository>()
        val subject = ObserveOrganizations(repository)

        should("emit empty list when repository has no organizations") {
            every { repository.observeOrganizations() } returns flowOf(emptyList())
            subject().first() shouldBe emptyList()
        }

        should("emit organizations from repository") {
            every { repository.observeOrganizations() } returns flowOf(listOf(TestOrganization))
            subject().first() shouldBe listOf(TestOrganization)
        }
    })
