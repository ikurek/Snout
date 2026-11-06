package com.nightcode.snout.shared.domain.usecase.errortracking

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.errortracking.ErrorTrackingRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.errortracking.TestExceptionOccurrences
import com.nightcode.snout.shared.domain.model.errortracking.TestOccurrenceExceptions
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@OptIn(TestData::class)
class GetOccurrenceExceptionsTest :
    ShouldSpec({
        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<ErrorTrackingRepository>()
        val subject = GetOccurrenceExceptions(GetSelectedProjectId(appStateRepository), repository)

        should("get the occurrence's exceptions in the selected project") {
            val occurrence = TestExceptionOccurrences.first()
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            everySuspend { repository.getOccurrenceExceptions(TestSelectedProject.projectId, occurrence) } returns
                Either.Right(TestOccurrenceExceptions)

            subject(occurrence) shouldBe Either.Right(TestOccurrenceExceptions)
        }
    })
