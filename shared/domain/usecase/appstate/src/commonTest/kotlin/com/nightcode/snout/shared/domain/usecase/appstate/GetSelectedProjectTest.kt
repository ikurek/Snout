package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@OptIn(TestData::class)
class GetSelectedProjectTest :
    ShouldSpec({
        val repository = mock<AppStateRepository>(autoUnit)
        val subject = GetSelectedProject(repository)

        should("return null when no project is selected") {
            everySuspend { repository.getSelectedProject() } returns null
            subject() shouldBe null
        }

        should("return selected project when one is set") {
            everySuspend { repository.getSelectedProject() } returns TestSelectedProject
            subject() shouldBe TestSelectedProject
        }
    })
