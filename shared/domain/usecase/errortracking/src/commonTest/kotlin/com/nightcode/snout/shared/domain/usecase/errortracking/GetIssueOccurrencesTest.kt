package com.nightcode.snout.shared.domain.usecase.errortracking

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.errortracking.ErrorTrackingRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.errortracking.TestExceptionOccurrences
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@OptIn(TestData::class)
class GetIssueOccurrencesTest :
    ShouldSpec({
        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<ErrorTrackingRepository>()
        val subject = GetIssueOccurrences(GetSelectedProjectId(appStateRepository), repository)

        should("get the issue's occurrences in the selected project") {
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            everySuspend { repository.getOccurrences(TestSelectedProject.projectId, "issue-1") } returns
                Either.Right(TestExceptionOccurrences)

            subject("issue-1") shouldBe Either.Right(TestExceptionOccurrences)
        }
    })
