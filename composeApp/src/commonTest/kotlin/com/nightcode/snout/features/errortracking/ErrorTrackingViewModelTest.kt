package com.nightcode.snout.features.errortracking

import arrow.core.Either
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.errortracking.ErrorTrackingRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter
import com.nightcode.snout.shared.domain.model.errortracking.TestAppVersions
import com.nightcode.snout.shared.domain.model.errortracking.TestErrorTrackingIssue
import com.nightcode.snout.shared.domain.model.errortracking.TestResolvedErrorTrackingIssue
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.errortracking.GetErrorAppVersions
import com.nightcode.snout.shared.domain.usecase.errortracking.ObserveErrorTrackingIssues
import com.nightcode.snout.shared.domain.usecase.errortracking.RefreshErrorTrackingIssues
import com.nightcode.snout.shared.domain.usecase.errortracking.SearchErrorTrackingIssues
import com.nightcode.snout.testing.subscribed
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Duration

@OptIn(TestData::class)
class ErrorTrackingViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<ErrorTrackingRepository>()

        fun subject(): ErrorTrackingViewModel {
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            every { appStateRepository.observeSelectedProject() } returns flowOf(TestSelectedProject)
            every { repository.observeIssues(any(), IssueStatusFilter.Active) } returns
                flowOf(listOf(TestErrorTrackingIssue))
            every { repository.observeIssues(any(), IssueStatusFilter.Resolved) } returns
                flowOf(listOf(TestResolvedErrorTrackingIssue))
            everySuspend { repository.refreshIssues(any(), any()) } returns Either.Right(Unit)
            everySuspend { repository.getAppVersions(any()) } returns Either.Right(TestAppVersions)
            val projectId = GetSelectedProjectId(appStateRepository)
            return ErrorTrackingViewModel(
                refreshIssues = RefreshErrorTrackingIssues(projectId, repository),
                searchIssues = SearchErrorTrackingIssues(projectId, repository),
                getAppVersions = GetErrorAppVersions(projectId, repository),
                observeIssues = ObserveErrorTrackingIssues(ObserveSelectedProjectId(appStateRepository), repository),
                observeSelectedProjectId = ObserveSelectedProjectId(appStateRepository),
                searchDebounce = Duration.ZERO,
            )
        }

        should("list cached active issues and search within the status") {
            val found = TestErrorTrackingIssue.copy(id = "found", name = "RangeError")
            everySuspend { repository.searchIssues(any(), IssueStatusFilter.Active, "range", null) } returns
                Either.Right(listOf(found))
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.uiState.value.issues shouldBe
                    CachedUiState.Content(listOf(TestErrorTrackingIssue), isRefreshing = false)
                viewModel.onQueryChanged("range")
                viewModel.uiState.value.issues.shouldBeInstanceOf<CachedUiState.Content<*>>().data shouldBe listOf(
                    found,
                )
            }
        }

        should("show and refresh the selected status") {
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.onStatusChanged(IssueStatusFilter.Resolved)
                viewModel.uiState.value.status shouldBe IssueStatusFilter.Resolved
                viewModel.uiState.value.issues shouldBe
                    CachedUiState.Content(listOf(TestResolvedErrorTrackingIssue), isRefreshing = false)
                verifySuspend { repository.refreshIssues(TestSelectedProject.projectId, IssueStatusFilter.Resolved) }
            }
        }

        should("offer the reported versions and fetch issues for the picked one") {
            val inVersion = TestErrorTrackingIssue.copy(occurrences = 40)
            everySuspend { repository.searchIssues(any(), IssueStatusFilter.Active, "", "2.13.2") } returns
                Either.Right(listOf(inVersion))
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.uiState.value.appVersions shouldBe TestAppVersions
                viewModel.onAppVersionChanged("2.13.2")
                viewModel.uiState.value.appVersion shouldBe "2.13.2"
                viewModel.uiState.value.issues.shouldBeInstanceOf<CachedUiState.Content<*>>().data shouldBe listOf(
                    inVersion,
                )
                viewModel.onAppVersionChanged(null)
                viewModel.uiState.value.issues shouldBe
                    CachedUiState.Content(listOf(TestErrorTrackingIssue), isRefreshing = false)
            }
        }
    })
