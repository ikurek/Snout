package com.nightcode.snout.features.logs

import arrow.core.Either
import com.nightcode.snout.common.state.RefreshableUiState
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
import com.nightcode.snout.shared.domain.usecase.logs.GetLogServices
import com.nightcode.snout.shared.domain.usecase.logs.ObserveLogServiceReloads
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
class LogsViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<LogRepository>()
        val selected = MutableStateFlow<SelectedProject?>(TestSelectedProject)

        fun subject(): LogsViewModel {
            selected.value = TestSelectedProject
            everySuspend { appStateRepository.getSelectedProject() } calls { selected.value }
            every { appStateRepository.observeSelectedProject() } returns selected
            everySuspend { repository.getServices(any(), LogsRange.LastHour) } returns
                Either.Right(TestLogServices.take(1))
            return LogsViewModel(
                getServices = GetLogServices(GetSelectedProjectId(appStateRepository), repository),
                observeLogServiceReloads = ObserveLogServiceReloads(ObserveSelectedProjectId(appStateRepository)),
            )
        }

        should("load the last day's services on start and switch ranges") {
            everySuspend { repository.getServices(any(), LogsRange.Last24Hours) } returns Either.Right(TestLogServices)
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.uiState.value.services shouldBe RefreshableUiState.Content(
                    TestLogServices,
                    isRefreshing = false,
                )
                viewModel.onRangeSelected(LogsRange.LastHour)
                viewModel.uiState.value.range shouldBe LogsRange.LastHour
                viewModel.uiState.value.services shouldBe
                    RefreshableUiState.Content(TestLogServices.take(1), isRefreshing = false)
                verifySuspend { repository.getServices(TestSelectedProject.projectId, LogsRange.LastHour) }
            }
        }

        should("keep the services and report a failed pull to refresh") {
            everySuspend { repository.getServices(any(), LogsRange.Last24Hours) } sequentially {
                returns(Either.Right(TestLogServices))
                returns(Either.Left(DataError.ConnectionError))
            }
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.refresh()
                viewModel.uiState.value.services shouldBe RefreshableUiState.Content(
                    TestLogServices,
                    isRefreshing = false,
                    refreshError = DataError.ConnectionError,
                )
            }
        }

        should("load the selected range again when another project is selected") {
            everySuspend { repository.getServices(any(), LogsRange.Last24Hours) } returns Either.Right(TestLogServices)
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.onRangeSelected(LogsRange.LastHour)
                selected.value = TestSelectedProject.copy(projectId = 2)
                verifySuspend { repository.getServices(2, LogsRange.LastHour) }
            }
        }
    })
