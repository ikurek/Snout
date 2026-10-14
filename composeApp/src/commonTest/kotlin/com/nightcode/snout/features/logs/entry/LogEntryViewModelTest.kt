package com.nightcode.snout.features.logs.entry

import arrow.core.Either
import com.nightcode.snout.navigation.LogEntryNavKey
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.logs.LogRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.logs.TestLogEntries
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.logs.GetLogEntry
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@OptIn(TestData::class)
class LogEntryViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<LogRepository>()
        val entry = TestLogEntries.first()
        val navKey = LogEntryNavKey(service = "checkout-api", id = entry.id, timestamp = entry.timestamp)

        fun subject() = LogEntryViewModel(
            navKey = navKey,
            getLogEntry = GetLogEntry(GetSelectedProjectId(appStateRepository), repository),
        )

        beforeTest { everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject }

        should("show the line looked up by its service, id and time") {
            everySuspend { repository.getLog(any(), "checkout-api", entry.id, entry.timestamp) } returns
                Either.Right(entry)
            subject().uiState.value shouldBe LogEntryUiState(entry = entry)
        }

        should("report a line that can no longer be found") {
            everySuspend { repository.getLog(any(), any(), any(), any()) } returns Either.Left(DataError.NotFound)
            subject().uiState.value shouldBe LogEntryUiState(error = DataError.NotFound)
        }
    })
