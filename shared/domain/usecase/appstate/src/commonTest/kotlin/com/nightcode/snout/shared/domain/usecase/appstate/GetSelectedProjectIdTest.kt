package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@OptIn(TestData::class)
class GetSelectedProjectIdTest :
    ShouldSpec({
        val repository = mock<AppStateRepository>()
        val subject = GetSelectedProjectId(repository)

        should("return selected project id") {
            everySuspend { repository.getSelectedProject() } returns TestSelectedProject
            subject() shouldBe TestSelectedProject.projectId
        }

        should("fail when no project is selected") {
            everySuspend { repository.getSelectedProject() } returns null
            shouldThrow<IllegalStateException> { subject() }
        }
    })
