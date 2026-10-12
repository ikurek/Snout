package com.nightcode.snout.features.logs

import arrow.core.Either
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.logs.LogRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.logs.LogsRange
import com.nightcode.snout.shared.domain.model.logs.TestLogServices
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.logs.ObserveLogServices
import com.nightcode.snout.shared.domain.usecase.logs.RefreshLogServices
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(TestData::class)
class LogsViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<LogRepository>()

        fun subject(): LogsViewModel {
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            every { appStateRepository.observeSelectedProject() } returns flowOf(TestSelectedProject)
            every { repository.observeServices(any(), LogsRange.Last24Hours) } returns flowOf(TestLogServices)
            every { repository.observeServices(any(), LogsRange.LastHour) } returns flowOf(TestLogServices.take(1))
            everySuspend { repository.refreshServices(any(), any()) } returns Either.Right(Unit)
            return LogsViewModel(
                refreshServices = RefreshLogServices(GetSelectedProjectId(appStateRepository), repository),
                observeServices = ObserveLogServices(ObserveSelectedProjectId(appStateRepository), repository),
                observeSelectedProjectId = ObserveSelectedProjectId(appStateRepository),
            )
        }

        should("show the last day's services and switch ranges") {
            val viewModel = subject()
            val collector = launch(Dispatchers.Main) { viewModel.uiState.collect {} }

            viewModel.uiState.value.services shouldBe CachedUiState.Content(TestLogServices, isRefreshing = false)
            viewModel.onRangeChange(LogsRange.LastHour)
            viewModel.uiState.value.range shouldBe LogsRange.LastHour
            viewModel.uiState.value.services shouldBe CachedUiState.Content(
                TestLogServices.take(1),
                isRefreshing = false,
            )
            verifySuspend { repository.refreshServices(TestSelectedProject.projectId, LogsRange.LastHour) }
            collector.cancel()
        }
    })
