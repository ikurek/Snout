package com.nightcode.snout.features.activitylog

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.activitylog.ActivityLogRepository
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.activitylog.ActivityEntry
import com.nightcode.snout.shared.domain.model.activitylog.TestActivityEntry
import com.nightcode.snout.shared.domain.model.appstate.SelectedProject
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.activitylog.GetActivityLog
import com.nightcode.snout.shared.domain.usecase.activitylog.ObserveActivityLogReloads
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(TestData::class)
class ActivityLogViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<ActivityLogRepository>()
        val selected = MutableStateFlow<SelectedProject?>(TestSelectedProject)
        val other = TestActivityEntry.copy(id = "a2")

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

            viewModel.uiState.value shouldBe ActivityLogUiState(entries = listOf(TestActivityEntry))
            verifySuspend { repository.getActivity(TestSelectedProject.projectId) }
        }

        should("keep the entries and report a failed pull to refresh") {
            everySuspend { repository.getActivity(any()) } sequentially {
                returns(Either.Right(listOf(TestActivityEntry)))
                returns(Either.Left(DataError.ConnectionError))
            }
            val viewModel = subject()

            viewModel.refresh()
            viewModel.uiState.value shouldBe ActivityLogUiState(
                entries = listOf(TestActivityEntry),
                error = DataError.ConnectionError,
            )
        }

        should("show the error when the first load fails, and the skeleton when retried") {
            val retried = CompletableDeferred<Either<DataError, List<ActivityEntry>>>()
            var loads = 0
            everySuspend { repository.getActivity(any()) } calls {
                loads++
                if (loads == 1) Either.Left(DataError.Unauthorized) else retried.await()
            }
            val viewModel = subject()
            viewModel.uiState.value shouldBe ActivityLogUiState(error = DataError.Unauthorized)

            viewModel.refresh()
            viewModel.uiState.value shouldBe ActivityLogUiState()
            retried.complete(Either.Right(listOf(TestActivityEntry)))
            viewModel.uiState.value shouldBe ActivityLogUiState(entries = listOf(TestActivityEntry))
        }

        should("show the empty state when the project has no activity") {
            everySuspend { repository.getActivity(any()) } returns Either.Right(emptyList())
            val viewModel = subject()

            viewModel.uiState.value shouldBe ActivityLogUiState(entries = emptyList())
        }

        should("start from the skeleton when another project is selected") {
            val otherProject = CompletableDeferred<Either<DataError, List<ActivityEntry>>>()
            everySuspend { repository.getActivity(TestSelectedProject.projectId) } returns
                Either.Right(listOf(TestActivityEntry))
            everySuspend { repository.getActivity(2) } calls { otherProject.await() }
            val viewModel = subject()

            selected.value = TestSelectedProject.copy(projectId = 2)
            viewModel.uiState.value shouldBe ActivityLogUiState()
            otherProject.complete(Either.Right(listOf(other)))
            viewModel.uiState.value shouldBe ActivityLogUiState(entries = listOf(other))
        }

        should("keep the entries on screen while refreshing, and apply only the latest of two refreshes") {
            val first = CompletableDeferred<Either<DataError, List<ActivityEntry>>>()
            val second = CompletableDeferred<Either<DataError, List<ActivityEntry>>>()
            var loads = 0
            everySuspend { repository.getActivity(any()) } calls {
                loads++
                when (loads) {
                    1 -> Either.Right(listOf(TestActivityEntry))
                    2 -> first.await()
                    else -> second.await()
                }
            }
            val viewModel = subject()

            viewModel.refresh()
            viewModel.uiState.value shouldBe ActivityLogUiState(
                entries = listOf(TestActivityEntry),
                isRefreshing = true,
            )
            viewModel.refresh()
            first.complete(Either.Right(emptyList()))
            viewModel.uiState.value.isRefreshing shouldBe true
            second.complete(Either.Right(listOf(other)))
            viewModel.uiState.value shouldBe ActivityLogUiState(entries = listOf(other))
        }

        should("refresh when the screen is entered again, but not the first time nor while a load runs") {
            val reloaded = CompletableDeferred<Either<DataError, List<ActivityEntry>>>()
            var loads = 0
            everySuspend { repository.getActivity(any()) } calls {
                loads++
                if (loads == 1) Either.Right(listOf(TestActivityEntry)) else reloaded.await()
            }
            val viewModel = subject()

            viewModel.onScreenEntered()
            loads shouldBe 1
            viewModel.onScreenEntered()
            loads shouldBe 2
            viewModel.onScreenEntered()
            loads shouldBe 2
            reloaded.complete(Either.Right(listOf(other)))
            viewModel.uiState.value shouldBe ActivityLogUiState(entries = listOf(other))
        }
    })
