package com.nightcode.snout.features.activitylog

import arrow.core.Either
import com.nightcode.snout.common.state.RefreshableUiState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.activitylog.ActivityLogRepository
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.activitylog.TestActivityEntry
import com.nightcode.snout.shared.domain.model.appstate.SelectedProject
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.activitylog.GetActivityLog
import com.nightcode.snout.shared.domain.usecase.activitylog.ObserveActivityLogReloads
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import com.nightcode.snout.testing.subscribed
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(TestData::class)
class ActivityLogViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<ActivityLogRepository>()
        val selected = MutableStateFlow<SelectedProject?>(TestSelectedProject)

        fun subject(): ActivityLogViewModel {
            selected.value = TestSelectedProject
            everySuspend { appStateRepository.getSelectedProject() } calls { selected.value }
            every { appStateRepository.observeSelectedProject() } returns selected
            return ActivityLogViewModel(
                getActivityLog = GetActivityLog(GetSelectedProjectId(appStateRepository), repository),
                observeActivityLogReloads = ObserveActivityLogReloads(ObserveSelectedProjectId(appStateRepository)),
            )
        }

        should("load the entries of the selected project on start") {
            everySuspend { repository.getActivity(any()) } returns Either.Right(listOf(TestActivityEntry))
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.uiState.value shouldBe RefreshableUiState.Content(
                    listOf(TestActivityEntry),
                    isRefreshing = false,
                )
                verifySuspend { repository.getActivity(TestSelectedProject.projectId) }
            }
        }

        should("keep the entries and report a failed pull to refresh") {
            everySuspend { repository.getActivity(any()) } sequentially {
                returns(Either.Right(listOf(TestActivityEntry)))
                returns(Either.Left(DataError.ConnectionError))
            }
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.refresh()
                viewModel.uiState.value shouldBe RefreshableUiState.Content(
                    listOf(TestActivityEntry),
                    isRefreshing = false,
                    refreshError = DataError.ConnectionError,
                )
            }
        }

        should("show the error when the first load fails") {
            everySuspend { repository.getActivity(any()) } returns Either.Left(DataError.Unauthorized)
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.uiState.value shouldBe RefreshableUiState.Error(DataError.Unauthorized)
            }
        }

        should("show the empty state when the project has no activity") {
            everySuspend { repository.getActivity(any()) } returns Either.Right(emptyList())
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.uiState.value shouldBe RefreshableUiState.Empty(isRefreshing = false)
            }
        }

        should("load again when another project is selected") {
            everySuspend { repository.getActivity(any()) } returns Either.Right(listOf(TestActivityEntry))
            val viewModel = subject()
            viewModel.uiState.subscribed {
                selected.value = TestSelectedProject.copy(projectId = 2)
                verifySuspend { repository.getActivity(2) }
            }
        }
    })
