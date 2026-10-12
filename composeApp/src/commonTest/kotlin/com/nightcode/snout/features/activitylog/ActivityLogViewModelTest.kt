package com.nightcode.snout.features.activitylog

import arrow.core.Either
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.activitylog.ActivityLogRepository
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.activitylog.ActivityEntry
import com.nightcode.snout.shared.domain.model.activitylog.TestActivityEntry
import com.nightcode.snout.shared.domain.model.appstate.SelectedProject
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.activitylog.ObserveActivityLog
import com.nightcode.snout.shared.domain.usecase.activitylog.RefreshActivityLog
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import com.nightcode.snout.testing.subscribed
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

@OptIn(TestData::class)
class ActivityLogViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<ActivityLogRepository>()
        val selected = MutableStateFlow<SelectedProject?>(TestSelectedProject)

        fun subject(cached: List<ActivityEntry>? = listOf(TestActivityEntry)): ActivityLogViewModel {
            everySuspend { appStateRepository.getSelectedProject() } calls { selected.value }
            every { appStateRepository.observeSelectedProject() } returns selected
            every { repository.observeActivity(any()) } returns flowOf(cached)
            return ActivityLogViewModel(
                refreshActivityLog = RefreshActivityLog(GetSelectedProjectId(appStateRepository), repository),
                observeActivityLog = ObserveActivityLog(ObserveSelectedProjectId(appStateRepository), repository),
                observeSelectedProjectId = ObserveSelectedProjectId(appStateRepository),
            )
        }

        should("refresh on start and list the cached entries") {
            everySuspend { repository.refreshActivity(any()) } returns Either.Right(Unit)
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.uiState.value shouldBe CachedUiState.Content(listOf(TestActivityEntry), isRefreshing = false)
                verifySuspend { repository.refreshActivity(TestSelectedProject.projectId) }
            }
        }

        should("keep the cached entries and report a failed refresh") {
            everySuspend { repository.refreshActivity(any()) } returns Either.Left(DataError.ConnectionError)
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.uiState.value shouldBe CachedUiState.Content(
                    listOf(TestActivityEntry),
                    isRefreshing = false,
                    refreshError = DataError.ConnectionError,
                )
            }
        }

        should("show the error when nothing is cached and the refresh fails") {
            everySuspend { repository.refreshActivity(any()) } returns Either.Left(DataError.Unauthorized)
            val viewModel = subject(cached = null)
            viewModel.uiState.subscribed {
                viewModel.uiState.value shouldBe CachedUiState.Error(DataError.Unauthorized)
            }
        }

        should("show the empty state when the project has no activity") {
            everySuspend { repository.refreshActivity(any()) } returns Either.Right(Unit)
            val viewModel = subject(cached = emptyList())
            viewModel.uiState.subscribed {
                viewModel.uiState.value shouldBe CachedUiState.Empty(isRefreshing = false)
            }
        }

        should("refresh again when another project is selected") {
            everySuspend { repository.refreshActivity(any()) } returns Either.Right(Unit)
            val viewModel = subject()
            viewModel.uiState.subscribed {
                selected.value = TestSelectedProject.copy(projectId = 2)
                verifySuspend { repository.refreshActivity(2) }
            }
        }
    })
