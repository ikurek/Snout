package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

@OptIn(TestData::class)
class ObserveSelectedProjectTest :
    ShouldSpec({
        val repository = mock<AppStateRepository>(autoUnit)
        val subject = ObserveSelectedProject(repository)

        should("emit null when no project is selected") {
            every { repository.observeSelectedProject() } returns flowOf(null)
            subject().first() shouldBe null
        }

        should("emit selected project when one is set") {
            every { repository.observeSelectedProject() } returns flowOf(TestSelectedProject)
            subject().first() shouldBe TestSelectedProject
        }
    })
