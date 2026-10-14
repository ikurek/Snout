package com.nightcode.snout.features.errortracking

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.errortracking.ErrorTrackingRepository
import com.nightcode.snout.shared.domain.model.appstate.SelectedProject
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.errortracking.ErrorTrackingIssue
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter
import com.nightcode.snout.shared.domain.model.errortracking.TestAppVersions
import com.nightcode.snout.shared.domain.model.errortracking.TestErrorTrackingIssue
import com.nightcode.snout.shared.domain.model.errortracking.TestResolvedErrorTrackingIssue
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.errortracking.GetErrorAppVersions
import com.nightcode.snout.shared.domain.usecase.errortracking.GetErrorTrackingIssues
import com.nightcode.snout.shared.domain.usecase.errortracking.ObserveErrorTrackingReloads
import com.nightcode.snout.shared.domain.usecase.errortracking.SearchErrorTrackingIssues
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Duration

@OptIn(TestData::class)
class ErrorTrackingViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<ErrorTrackingRepository>()
        val selected = MutableStateFlow<SelectedProject?>(TestSelectedProject)
        val changes = MutableSharedFlow<Unit>()
        val active = Either.Right(listOf(TestErrorTrackingIssue))

        fun subject(): ErrorTrackingViewModel {
            selected.value = TestSelectedProject
            everySuspend { appStateRepository.getSelectedProject() } calls { selected.value }
            every { appStateRepository.observeSelectedProject() } returns selected
            every { repository.changes } returns changes
            everySuspend { repository.getIssues(any(), IssueStatusFilter.Resolved) } returns
                Either.Right(listOf(TestResolvedErrorTrackingIssue))
            everySuspend { repository.getAppVersions(any()) } returns Either.Right(TestAppVersions)
            val projectId = GetSelectedProjectId(appStateRepository)
            return ErrorTrackingViewModel(
                getIssues = GetErrorTrackingIssues(projectId, repository),
                searchIssues = SearchErrorTrackingIssues(projectId, repository),
                getAppVersions = GetErrorAppVersions(projectId, repository),
                observeErrorTrackingReloads = ObserveErrorTrackingReloads(
                    ObserveSelectedProjectId(appStateRepository),
                    repository,
                ),
                searchDebounce = Duration.ZERO,
            )
        }

        should("load active issues on start and search within the status") {
            everySuspend { repository.getIssues(any(), IssueStatusFilter.Active) } returns active
            val found = TestErrorTrackingIssue.copy(id = "found", name = "RangeError")
            everySuspend { repository.searchIssues(any(), IssueStatusFilter.Active, "range", null) } returns
                Either.Right(listOf(found))
            val viewModel = subject()
            viewModel.uiState.value.issues shouldBe listOf(TestErrorTrackingIssue)
            viewModel.onQueryChanged("range")
            viewModel.uiState.value.isFiltered shouldBe true
            viewModel.uiState.value.filteredIssues shouldBe listOf(found)
        }

        should("load the selected status") {
            everySuspend { repository.getIssues(any(), IssueStatusFilter.Active) } returns active
            val viewModel = subject()
            viewModel.onStatusChanged(IssueStatusFilter.Resolved)
            viewModel.uiState.value.status shouldBe IssueStatusFilter.Resolved
            viewModel.uiState.value.issues shouldBe listOf(TestResolvedErrorTrackingIssue)
            verifySuspend { repository.getIssues(TestSelectedProject.projectId, IssueStatusFilter.Resolved) }
        }

        should("offer the reported versions and fetch issues for the picked one") {
            everySuspend { repository.getIssues(any(), IssueStatusFilter.Active) } returns active
            val inVersion = TestErrorTrackingIssue.copy(occurrences = 40)
            everySuspend { repository.searchIssues(any(), IssueStatusFilter.Active, "", "2.13.2") } returns
                Either.Right(listOf(inVersion))
            val viewModel = subject()
            viewModel.uiState.value.appVersions shouldBe TestAppVersions
            viewModel.onAppVersionChanged("2.13.2")
            viewModel.uiState.value.appVersion shouldBe "2.13.2"
            viewModel.uiState.value.filteredIssues shouldBe listOf(inVersion)
            viewModel.onAppVersionChanged(null)
            viewModel.uiState.value.isFiltered shouldBe false
            viewModel.uiState.value.issues shouldBe listOf(TestErrorTrackingIssue)
        }

        should("keep the issues and report a failed pull to refresh") {
            everySuspend { repository.getIssues(any(), IssueStatusFilter.Active) } sequentially {
                returns(active)
                returns(Either.Left(DataError.ConnectionError))
            }
            val viewModel = subject()
            viewModel.refresh()
            viewModel.uiState.value.issues shouldBe listOf(TestErrorTrackingIssue)
            viewModel.uiState.value.error shouldBe DataError.ConnectionError
        }

        should("load again after an issue's status changed in the app") {
            everySuspend { repository.getIssues(any(), IssueStatusFilter.Active) } sequentially {
                returns(active)
                returns(Either.Right(emptyList()))
            }
            val viewModel = subject()
            changes.emit(Unit)
            viewModel.uiState.value.issues shouldBe emptyList()
        }

        should("load again when another project is selected") {
            everySuspend { repository.getIssues(any(), IssueStatusFilter.Active) } returns active
            everySuspend { repository.searchIssues(any(), any(), any(), any()) } returns Either.Right(emptyList())
            val viewModel = subject()
            viewModel.onAppVersionChanged("2.13.2")
            selected.value = TestSelectedProject.copy(projectId = 2)
            verifySuspend { repository.getIssues(2, IssueStatusFilter.Active) }
            verifySuspend { repository.getAppVersions(2) }
            // Versions belong to a project, so the filter is dropped.
            viewModel.uiState.value.appVersion shouldBe null
        }

        should("fetch the filtered issues again on pull to refresh, from the skeleton") {
            everySuspend { repository.getIssues(any(), IssueStatusFilter.Active) } returns active
            val searched = CompletableDeferred<Either<DataError, List<ErrorTrackingIssue>>>()
            var searches = 0
            everySuspend { repository.searchIssues(any(), IssueStatusFilter.Active, "range", null) } calls {
                searches++
                if (searches == 1) Either.Right(listOf(TestErrorTrackingIssue)) else searched.await()
            }
            val viewModel = subject()
            viewModel.onQueryChanged("range")

            viewModel.refresh()
            viewModel.uiState.value.filteredIssues shouldBe null
            searched.complete(Either.Left(DataError.ConnectionError))
            viewModel.uiState.value.filterError shouldBe DataError.ConnectionError
            verifySuspend(exactly(1)) { repository.getIssues(any(), IssueStatusFilter.Active) }
        }
    })
