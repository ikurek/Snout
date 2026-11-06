package com.nightcode.snout.shared.domain.usecase.errortracking

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.errortracking.ErrorTrackingRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@OptIn(TestData::class)
class AssignErrorTrackingIssueTest :
    ShouldSpec({
        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<ErrorTrackingRepository>()
        val subject = AssignErrorTrackingIssue(GetSelectedProjectId(appStateRepository), repository)

        beforeTest { everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject }

        should("assign the issue in the selected project") {
            everySuspend { repository.assign(TestSelectedProject.projectId, "issue-1", 7) } returns Either.Right(Unit)

            subject("issue-1", userId = 7) shouldBe Either.Right(Unit)
        }

        should("pass a failed unassignment on") {
            everySuspend { repository.assign(TestSelectedProject.projectId, "issue-1", null) } returns
                Either.Left(DataError.Forbidden)

            subject("issue-1", userId = null) shouldBe Either.Left(DataError.Forbidden)
        }
    })
