package com.nightcode.snout.features.errortracking.detail

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.errortracking.ErrorTrackingRepository
import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.errortracking.IssueAssignee
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatus
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter
import com.nightcode.snout.shared.domain.model.errortracking.TestErrorTrackingIssue
import com.nightcode.snout.shared.domain.model.errortracking.TestExceptionOccurrences
import com.nightcode.snout.shared.domain.model.errortracking.TestOccurrenceExceptions
import com.nightcode.snout.shared.domain.model.organization.TestOrganizationMembers
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProject
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.errortracking.AssignErrorTrackingIssue
import com.nightcode.snout.shared.domain.usecase.errortracking.GetErrorTrackingIssue
import com.nightcode.snout.shared.domain.usecase.errortracking.GetIssueOccurrences
import com.nightcode.snout.shared.domain.usecase.errortracking.GetOccurrenceExceptions
import com.nightcode.snout.shared.domain.usecase.errortracking.SetErrorTrackingIssueStatus
import com.nightcode.snout.shared.domain.usecase.organization.GetOrganizationMembers
import com.nightcode.snout.shared.domain.usecase.telemetry.TrackIssueStatusChanged
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first

@OptIn(TestData::class)
class ErrorTrackingIssueViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val telemetryRepository = mock<TelemetryRepository>(MockMode.autoUnit)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<ErrorTrackingRepository>()
        val organizationRepository = mock<OrganizationRepository>()
        val latest = TestExceptionOccurrences[0]
        val older = TestExceptionOccurrences[1]
        val olderExceptions = TestOccurrenceExceptions.map { it.copy(type = "RangeError") }

        fun subject(): ErrorTrackingIssueViewModel {
            val projectId = GetSelectedProjectId(appStateRepository)
            return ErrorTrackingIssueViewModel(
                issueId = TestErrorTrackingIssue.id,
                getIssue = GetErrorTrackingIssue(projectId, repository),
                setIssueStatus = SetErrorTrackingIssueStatus(projectId, repository),
                getOccurrences = GetIssueOccurrences(projectId, repository),
                getOccurrenceExceptions = GetOccurrenceExceptions(projectId, repository),
                assignIssue = AssignErrorTrackingIssue(projectId, repository),
                getMembers = GetOrganizationMembers(GetSelectedProject(appStateRepository), organizationRepository),
                trackIssueStatusChanged = TrackIssueStatusChanged(telemetryRepository),
            )
        }

        beforeTest {
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            everySuspend { repository.getIssue(any(), TestErrorTrackingIssue.id) } returns
                Either.Right(TestErrorTrackingIssue)
            everySuspend { repository.getOccurrences(any(), TestErrorTrackingIssue.id) } returns
                Either.Right(TestExceptionOccurrences)
            everySuspend { repository.getOccurrenceExceptions(any(), latest) } returns
                Either.Right(TestOccurrenceExceptions)
            everySuspend { repository.getOccurrenceExceptions(any(), older) } returns Either.Right(olderExceptions)
            everySuspend { organizationRepository.getMembers(any()) } returns Either.Right(TestOrganizationMembers)
        }

        should("load the issue, its occurrences, the latest one's stack trace and the members on start") {
            subject().uiState.value shouldBe TestErrorTrackingIssueUiState
        }

        should("show the error and recover on retry") {
            everySuspend { repository.getIssue(any(), TestErrorTrackingIssue.id) } sequentially {
                returns(Either.Left(DataError.ConnectionError))
                returns(Either.Right(TestErrorTrackingIssue))
            }
            val viewModel = subject()
            viewModel.uiState.value.error shouldBe DataError.ConnectionError
            viewModel.refresh()
            viewModel.uiState.value.issue shouldBe TestErrorTrackingIssue
        }

        should("resolve the issue and offer to reopen it") {
            everySuspend {
                repository.setStatus(any(), TestErrorTrackingIssue.id, IssueStatusFilter.Resolved)
            } returns Either.Right(IssueStatus.Resolved)
            val viewModel = subject()

            viewModel.changeStatus(IssueStatusFilter.Resolved)

            viewModel.sideEffects.first() shouldBe
                ErrorTrackingIssueSideEffect.StatusChanged(IssueStatusFilter.Resolved, undo = IssueStatusFilter.Active)
            viewModel.uiState.value shouldBe TestErrorTrackingIssueUiState.copy(
                issue = TestErrorTrackingIssue.copy(status = IssueStatus.Resolved),
            )
        }

        should("keep the status and report a key without the write scope") {
            everySuspend { repository.setStatus(any(), any(), any()) } returns Either.Left(DataError.Forbidden)
            val viewModel = subject()

            viewModel.changeStatus(IssueStatusFilter.Suppressed)

            viewModel.sideEffects.first() shouldBe ErrorTrackingIssueSideEffect.StatusChangeFailed(DataError.Forbidden)
            viewModel.uiState.value shouldBe TestErrorTrackingIssueUiState
        }

        should("show another occurrence's stack trace when it is picked") {
            val viewModel = subject()

            viewModel.onOccurrenceSelected(older.id)

            viewModel.uiState.value.selectedOccurrence shouldBe older
            viewModel.uiState.value.exceptions shouldBe olderExceptions
        }

        should("keep the picked occurrence on refresh without loading its stack trace again") {
            val viewModel = subject()
            viewModel.onOccurrenceSelected(older.id)

            viewModel.refresh()

            viewModel.uiState.value.selectedOccurrenceId shouldBe older.id
            verifySuspend(exactly(1)) { repository.getOccurrenceExceptions(any(), older) }
        }

        should("show the newest occurrence once the picked one is no longer among the latest") {
            val viewModel = subject()
            viewModel.onOccurrenceSelected(older.id)
            everySuspend { repository.getOccurrences(any(), TestErrorTrackingIssue.id) } returns
                Either.Right(listOf(latest))

            viewModel.refresh()

            viewModel.uiState.value.selectedOccurrenceId shouldBe latest.id
            viewModel.uiState.value.exceptions shouldBe TestOccurrenceExceptions
        }

        should("keep the issue usable when its occurrences don't load") {
            everySuspend { repository.getOccurrences(any(), any()) } returns Either.Left(DataError.Forbidden)

            subject().uiState.value shouldBe TestErrorTrackingIssueUiState.copy(
                occurrences = null,
                occurrencesError = DataError.Forbidden,
                selectedOccurrenceId = null,
                exceptions = null,
            )
        }

        should("show a stack trace that failed to load once it is retried") {
            everySuspend { repository.getOccurrenceExceptions(any(), latest) } sequentially {
                returns(Either.Left(DataError.ConnectionError))
                returns(Either.Right(TestOccurrenceExceptions))
            }
            val viewModel = subject()
            viewModel.uiState.value.exceptionsError shouldBe DataError.ConnectionError

            viewModel.onExceptionsRetried()

            viewModel.uiState.value shouldBe TestErrorTrackingIssueUiState
        }

        should("open the assignee picker and close it") {
            val viewModel = subject()

            viewModel.onAssigneeClicked()
            viewModel.uiState.value.isAssigneePickerVisible shouldBe true

            viewModel.onAssigneePickerDismissed()
            viewModel.uiState.value.isAssigneePickerVisible shouldBe false
        }

        should("assign the issue to another member and offer to give it back") {
            everySuspend { repository.assign(any(), TestErrorTrackingIssue.id, 9) } returns Either.Right(Unit)
            val viewModel = subject()
            viewModel.onAssigneeClicked()

            viewModel.onAssigneeSelected(9)

            viewModel.sideEffects.first() shouldBe ErrorTrackingIssueSideEffect.AssigneeChanged(
                userId = 9,
                assigneeName = "Grace Hopper",
                undo = AssigneeUndo(userId = 7),
            )
            viewModel.uiState.value shouldBe TestErrorTrackingIssueUiState.copy(
                issue = TestErrorTrackingIssue.copy(assignee = IssueAssignee.User(9)),
            )
        }

        should("offer to unassign again after assigning an unassigned issue") {
            everySuspend { repository.getIssue(any(), any()) } returns
                Either.Right(TestErrorTrackingIssue.copy(assignee = null))
            everySuspend { repository.assign(any(), any(), 7) } returns Either.Right(Unit)
            val viewModel = subject()

            viewModel.onAssigneeSelected(7)

            viewModel.sideEffects.first() shouldBe ErrorTrackingIssueSideEffect.AssigneeChanged(
                userId = 7,
                assigneeName = "Ada Lovelace",
                undo = AssigneeUndo(userId = null),
            )
        }

        should("not offer to undo replacing a role, which the app can't assign") {
            everySuspend { repository.getIssue(any(), any()) } returns
                Either.Right(TestErrorTrackingIssue.copy(assignee = IssueAssignee.Role("role-1")))
            everySuspend { repository.assign(any(), any(), null) } returns Either.Right(Unit)
            val viewModel = subject()

            viewModel.onAssigneeSelected(null)

            viewModel.sideEffects.first() shouldBe
                ErrorTrackingIssueSideEffect.AssigneeChanged(userId = null, assigneeName = null, undo = null)
            viewModel.uiState.value.issue?.assignee shouldBe null
        }

        should("keep the assignee and report a key without the write scope") {
            everySuspend { repository.assign(any(), any(), any()) } returns Either.Left(DataError.Forbidden)
            val viewModel = subject()

            viewModel.onAssigneeSelected(null)

            viewModel.sideEffects.first() shouldBe ErrorTrackingIssueSideEffect.AssignFailed(DataError.Forbidden)
            viewModel.uiState.value shouldBe TestErrorTrackingIssueUiState
        }

        should("say why the picker can't open and load the members again") {
            everySuspend { organizationRepository.getMembers(any()) } sequentially {
                returns(Either.Left(DataError.Forbidden))
                returns(Either.Right(TestOrganizationMembers))
            }
            val viewModel = subject()

            viewModel.onAssigneeClicked()

            viewModel.sideEffects.first() shouldBe ErrorTrackingIssueSideEffect.MembersUnavailable(DataError.Forbidden)
            viewModel.uiState.value shouldBe TestErrorTrackingIssueUiState
        }
    })
