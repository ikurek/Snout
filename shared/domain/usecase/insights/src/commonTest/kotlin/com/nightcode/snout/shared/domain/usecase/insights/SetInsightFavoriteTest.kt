package com.nightcode.snout.shared.domain.usecase.insights

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.insights.InsightRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.insights.TestInsightSummary
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@OptIn(TestData::class)
class SetInsightFavoriteTest :
    ShouldSpec({
        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<InsightRepository>()
        val subject = SetInsightFavorite(GetSelectedProjectId(appStateRepository), repository)

        should("toggle favorite within the selected project") {
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            everySuspend { repository.setFavorite(TestSelectedProject.projectId, TestInsightSummary.id, false) } returns
                Either.Right(TestInsightSummary.copy(favorited = false))
            subject(TestInsightSummary.id, false).shouldBeRight().favorited shouldBe false
        }
    })
