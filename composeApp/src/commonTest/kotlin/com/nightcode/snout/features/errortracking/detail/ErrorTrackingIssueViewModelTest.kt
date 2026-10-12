package com.nightcode.snout.features.errortracking.detail

import arrow.core.Either
import com.nightcode.snout.common.state.LoadState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.errortracking.ErrorTrackingRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatus
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter
import com.nightcode.snout.shared.domain.model.errortracking.TestErrorTrackingIssue
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.errortracking.GetErrorTrackingIssue
import com.nightcode.snout.shared.domain.usecase.errortracking.SetErrorTrackingIssueStatus
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first

@OptIn(TestData::class)
class ErrorTrackingIssueViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<ErrorTrackingRepository>()

        fun subject(): ErrorTrackingIssueViewModel {
            val projectId = GetSelectedProjectId(appStateRepository)
            return ErrorTrackingIssueViewModel(
                issueId = TestErrorTrackingIssue.id,
                getIssue = GetErrorTrackingIssue(projectId, repository),
                setIssueStatus = SetErrorTrackingIssueStatus(projectId, repository),
            )
        }

        beforeTest { everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject }

        should("load the issue on start") {
            everySuspend { repository.getIssue(any(), TestErrorTrackingIssue.id) } returns
                Either.Right(TestErrorTrackingIssue)
            subject().uiState.value.issue shouldBe LoadState.Loaded(TestErrorTrackingIssue)
        }

        should("show the error and recover on retry") {
            everySuspend { repository.getIssue(any(), TestErrorTrackingIssue.id) } sequentially {
                returns(Either.Left(DataError.ConnectionError))
                returns(Either.Right(TestErrorTrackingIssue))
            }
            val viewModel = subject()
            viewModel.uiState.value.issue shouldBe LoadState.Error(DataError.ConnectionError)
            viewModel.refresh()
            viewModel.uiState.value.issue shouldBe LoadState.Loaded(TestErrorTrackingIssue)
        }

        should("resolve the issue and offer to reopen it") {
            everySuspend { repository.getIssue(any(), TestErrorTrackingIssue.id) } returns
                Either.Right(TestErrorTrackingIssue)
            everySuspend {
                repository.setStatus(any(), TestErrorTrackingIssue.id, IssueStatusFilter.Resolved)
            } returns Either.Right(IssueStatus.Resolved)
            val viewModel = subject()

            viewModel.changeStatus(IssueStatusFilter.Resolved)

            viewModel.sideEffects.first() shouldBe
                ErrorTrackingIssueSideEffect.StatusChanged(IssueStatusFilter.Resolved, undo = IssueStatusFilter.Active)
            viewModel.uiState.value shouldBe ErrorTrackingIssueUiState(
                issue = LoadState.Loaded(TestErrorTrackingIssue.copy(status = IssueStatus.Resolved)),
            )
        }

        should("keep the status and report a key without the write scope") {
            everySuspend { repository.getIssue(any(), TestErrorTrackingIssue.id) } returns
                Either.Right(TestErrorTrackingIssue)
            everySuspend { repository.setStatus(any(), any(), any()) } returns Either.Left(DataError.Forbidden)
            val viewModel = subject()

            viewModel.changeStatus(IssueStatusFilter.Suppressed)

            viewModel.sideEffects.first() shouldBe ErrorTrackingIssueSideEffect.StatusChangeFailed(DataError.Forbidden)
            viewModel.uiState.value shouldBe ErrorTrackingIssueUiState(
                issue = LoadState.Loaded(TestErrorTrackingIssue),
            )
        }
    })
