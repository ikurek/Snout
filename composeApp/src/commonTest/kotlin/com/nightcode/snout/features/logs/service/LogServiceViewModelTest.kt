package com.nightcode.snout.features.logs.service

import arrow.core.Either
import com.nightcode.snout.navigation.LogServiceNavKey
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.logs.LogRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.logs.LogPage
import com.nightcode.snout.shared.domain.model.logs.LogSeverityFilter
import com.nightcode.snout.shared.domain.model.logs.LogsRange
import com.nightcode.snout.shared.domain.model.logs.TestLogEntries
import com.nightcode.snout.shared.domain.model.logs.TestLogVolume
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.logs.GetServiceLogVolume
import com.nightcode.snout.shared.domain.usecase.logs.GetServiceLogs
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Duration

private const val Service = "checkout-api"

@OptIn(TestData::class)
class LogServiceViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<LogRepository>()
        val firstPage = TestLogEntries.take(2)
        val secondPage = TestLogEntries.drop(2)

        fun subject(): LogServiceViewModel {
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            everySuspend {
                repository.getLogs(any(), Service, LogsRange.LastHour, LogSeverityFilter.All, "", null)
            } returns Either.Right(LogPage(firstPage, nextCursor = "page-2"))
            everySuspend { repository.getVolume(any(), Service, LogsRange.LastHour, any(), any()) } returns
                Either.Right(TestLogVolume)
            val projectId = GetSelectedProjectId(appStateRepository)
            return LogServiceViewModel(
                navKey = LogServiceNavKey(Service, LogsRange.LastHour),
                getLogs = GetServiceLogs(projectId, repository),
                getVolume = GetServiceLogVolume(projectId, repository),
                searchDebounce = Duration.ZERO,
            )
        }

        should("load the first page and append the next one at the end of the list") {
            everySuspend {
                repository.getLogs(any(), Service, LogsRange.LastHour, LogSeverityFilter.All, "", "page-2")
            } returns Either.Right(LogPage(secondPage, nextCursor = null))
            val viewModel = subject()

            viewModel.uiState.value.logs shouldBe firstPage
            viewModel.uiState.value.volume shouldBe TestLogVolume
            viewModel.uiState.value.loadMore shouldBe LoadMoreStatus.Idle
            viewModel.loadMore()
            viewModel.uiState.value.logs shouldBe TestLogEntries
            viewModel.uiState.value.loadMore shouldBe LoadMoreStatus.Done
        }

        should("keep the loaded lines when the next page fails") {
            everySuspend {
                repository.getLogs(any(), Service, LogsRange.LastHour, LogSeverityFilter.All, "", "page-2")
            } returns Either.Left(DataError.RateLimited)
            val viewModel = subject()

            viewModel.loadMore()
            viewModel.uiState.value.logs shouldBe firstPage
            viewModel.uiState.value.loadMore shouldBe LoadMoreStatus.Failed(DataError.RateLimited)
        }

        should("start over when the severity filter changes") {
            everySuspend {
                repository.getLogs(any(), Service, LogsRange.LastHour, LogSeverityFilter.Errors, "", null)
            } returns Either.Right(LogPage(TestLogEntries.take(1), nextCursor = null))
            val viewModel = subject()

            viewModel.onSeverityChanged(LogSeverityFilter.Errors)
            viewModel.uiState.value.severity shouldBe LogSeverityFilter.Errors
            viewModel.uiState.value.logs shouldBe TestLogEntries.take(1)
            viewModel.uiState.value.loadMore shouldBe LoadMoreStatus.Done
        }

        should("hide the volume chart when it fails and still show the lines") {
            val viewModel = subject()
            everySuspend { repository.getVolume(any(), any(), any(), any(), any()) } returns
                Either.Left(DataError.UnexpectedError)

            viewModel.refresh()
            viewModel.uiState.value.volume shouldBe null
            viewModel.uiState.value.logs shouldBe firstPage
        }

        should("reload the lines when the screen is entered again, but not the first time") {
            val viewModel = subject()
            everySuspend {
                repository.getLogs(any(), Service, LogsRange.LastHour, LogSeverityFilter.All, "", null)
            } returns Either.Right(LogPage(secondPage, nextCursor = null))

            viewModel.onScreenEntered()
            viewModel.uiState.value.logs shouldBe firstPage
            viewModel.onScreenEntered()
            viewModel.uiState.value.logs shouldBe secondPage
        }

        should("keep the lines under a banner when a refresh fails") {
            val viewModel = subject()
            everySuspend {
                repository.getLogs(any(), Service, LogsRange.LastHour, LogSeverityFilter.All, "", null)
            } returns Either.Left(DataError.ConnectionError)

            viewModel.refresh()
            viewModel.uiState.value.logs shouldBe firstPage
            viewModel.uiState.value.error shouldBe DataError.ConnectionError
        }

        should("load the lines matching a typed query") {
            everySuspend {
                repository.getLogs(any(), Service, LogsRange.LastHour, LogSeverityFilter.All, "timeout", null)
            } returns Either.Right(LogPage(secondPage, nextCursor = null))
            val viewModel = subject()

            viewModel.onQueryChanged("timeout")
            viewModel.uiState.value.query shouldBe "timeout"
            viewModel.uiState.value.logs shouldBe secondPage
            viewModel.uiState.value.loadMore shouldBe LoadMoreStatus.Done
        }
    })
