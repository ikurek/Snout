package com.nightcode.snout.features.logs

import arrow.core.Either
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.logs.LogRepository
import com.nightcode.snout.shared.domain.model.appstate.SelectedProject
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.logs.LogsRange
import com.nightcode.snout.shared.domain.model.logs.TestLogServices
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.logs.ObserveLogServices
import com.nightcode.snout.shared.domain.usecase.logs.RefreshLogServices
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
class LogsViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<LogRepository>()

        val selected = MutableStateFlow<SelectedProject?>(TestSelectedProject)

        fun subject(refreshResult: Either<DataError, Unit> = Either.Right(Unit)): LogsViewModel {
            everySuspend { appStateRepository.getSelectedProject() } calls { selected.value }
            every { appStateRepository.observeSelectedProject() } returns selected
            every { repository.observeServices(any(), LogsRange.Last24Hours) } returns flowOf(TestLogServices)
            every { repository.observeServices(any(), LogsRange.LastHour) } returns flowOf(TestLogServices.take(1))
            everySuspend { repository.refreshServices(any(), any()) } returns refreshResult
            return LogsViewModel(
                refreshServices = RefreshLogServices(GetSelectedProjectId(appStateRepository), repository),
                observeServices = ObserveLogServices(ObserveSelectedProjectId(appStateRepository), repository),
                observeSelectedProjectId = ObserveSelectedProjectId(appStateRepository),
            )
        }

        should("show the last day's services and switch ranges") {
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.uiState.value.services shouldBe CachedUiState.Content(TestLogServices, isRefreshing = false)
                viewModel.onRangeSelected(LogsRange.LastHour)
                viewModel.uiState.value.range shouldBe LogsRange.LastHour
                viewModel.uiState.value.services shouldBe CachedUiState.Content(
                    TestLogServices.take(1),
                    isRefreshing = false,
                )
                verifySuspend { repository.refreshServices(TestSelectedProject.projectId, LogsRange.LastHour) }
            }
        }

        should("keep the cached services and report a failed refresh") {
            val viewModel = subject(refreshResult = Either.Left(DataError.ConnectionError))
            viewModel.uiState.subscribed {
                viewModel.uiState.value.services shouldBe CachedUiState.Content(
                    TestLogServices,
                    isRefreshing = false,
                    refreshError = DataError.ConnectionError,
                )
            }
        }

        should("refresh the selected range again when another project is selected") {
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.onRangeSelected(LogsRange.LastHour)
                selected.value = TestSelectedProject.copy(projectId = 2)
                verifySuspend { repository.refreshServices(2, LogsRange.LastHour) }
            }
        }
    })
