package com.nightcode.snout.features.errortracking.detail

import arrow.core.Either
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.errortracking.ErrorTrackingRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.errortracking.TestErrorTrackingIssue
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.errortracking.GetErrorTrackingIssue
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@OptIn(TestData::class)
class ErrorTrackingIssueViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<ErrorTrackingRepository>()

        fun subject() = ErrorTrackingIssueViewModel(
            issueId = TestErrorTrackingIssue.id,
            getIssue = GetErrorTrackingIssue(GetSelectedProjectId(appStateRepository), repository),
        )

        beforeTest { everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject }

        should("load the issue on start") {
            everySuspend { repository.getIssue(any(), TestErrorTrackingIssue.id) } returns
                Either.Right(TestErrorTrackingIssue)
            subject().uiState.value shouldBe CachedUiState.Content(TestErrorTrackingIssue, isRefreshing = false)
        }

        should("show the error and recover on retry") {
            everySuspend { repository.getIssue(any(), TestErrorTrackingIssue.id) } sequentially {
                returns(Either.Left(DataError.ConnectionError))
                returns(Either.Right(TestErrorTrackingIssue))
            }
            val viewModel = subject()
            viewModel.uiState.value shouldBe CachedUiState.Error(DataError.ConnectionError)
            viewModel.load()
            viewModel.uiState.value shouldBe CachedUiState.Content(TestErrorTrackingIssue, isRefreshing = false)
        }
    })
