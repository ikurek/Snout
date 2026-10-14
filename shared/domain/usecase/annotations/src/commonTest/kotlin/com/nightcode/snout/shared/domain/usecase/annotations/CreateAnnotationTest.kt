package com.nightcode.snout.shared.domain.usecase.annotations

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.annotations.AnnotationRepository
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.annotations.TestAnnotation
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.not
import dev.mokkery.verifySuspend
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec

@OptIn(TestData::class)
class CreateAnnotationTest :
    ShouldSpec({
        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<AnnotationRepository>()
        val subject = CreateAnnotation(GetSelectedProjectId(appStateRepository), repository)
        val dateMarker = TestAnnotation.dateMarker

        should("create the trimmed annotation in the selected project") {
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            everySuspend {
                repository.createAnnotation(TestSelectedProject.projectId, TestAnnotation.content, dateMarker)
            } returns Either.Right(TestAnnotation)
            subject("  ${TestAnnotation.content}\n", dateMarker) shouldBeRight TestAnnotation
            verifySuspend {
                repository.createAnnotation(TestSelectedProject.projectId, TestAnnotation.content, dateMarker)
            }
        }

        should("surface the repository error") {
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            everySuspend {
                repository.createAnnotation(TestSelectedProject.projectId, TestAnnotation.content, dateMarker)
            } returns Either.Left(DataError.Forbidden)
            subject(TestAnnotation.content, dateMarker) shouldBeLeft CreateAnnotationError.Failed(DataError.Forbidden)
        }

        should("refuse blank content without calling the repository") {
            // Its own repository, so the other tests' calls don't count.
            val untouched = mock<AnnotationRepository>()
            val blankSubject = CreateAnnotation(GetSelectedProjectId(appStateRepository), untouched)
            blankSubject(" \n\t", dateMarker) shouldBeLeft CreateAnnotationError.ContentIsBlank
            verifySuspend(not) { untouched.createAnnotation(any(), any(), any()) }
        }

        should("fail without a selected project") {
            everySuspend { appStateRepository.getSelectedProject() } returns null
            shouldThrow<IllegalStateException> { subject(TestAnnotation.content, dateMarker) }
        }
    })
