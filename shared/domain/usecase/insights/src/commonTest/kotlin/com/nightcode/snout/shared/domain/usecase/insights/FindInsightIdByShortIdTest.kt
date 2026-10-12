package com.nightcode.snout.shared.domain.usecase.insights

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.insights.InsightRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@OptIn(TestData::class)
class FindInsightIdByShortIdTest :
    ShouldSpec({
        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<InsightRepository>()
        val subject = FindInsightIdByShortId(GetSelectedProjectId(appStateRepository), repository)

        beforeTest {
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
        }

        should("look the short id up within the selected project") {
            everySuspend { repository.findInsightId(TestSelectedProject.projectId, "aBc123") } returns Either.Right(42L)
            subject("aBc123").shouldBeRight() shouldBe 42L
        }

        should("pass on a failed lookup") {
            everySuspend { repository.findInsightId(TestSelectedProject.projectId, "aBc123") } returns
                Either.Left(DataError.NotFound)
            subject("aBc123").shouldBeLeft() shouldBe DataError.NotFound
        }
    })
