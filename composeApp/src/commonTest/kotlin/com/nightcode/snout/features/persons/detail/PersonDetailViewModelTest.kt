package com.nightcode.snout.features.persons.detail

import arrow.core.Either
import com.nightcode.snout.designsystem.organisms.LoadMoreStatus
import com.nightcode.snout.navigation.PersonDetailNavKey
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.persons.PersonRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.persons.PersonEventPage
import com.nightcode.snout.shared.domain.model.persons.TestPerson
import com.nightcode.snout.shared.domain.model.persons.TestPersonEvents
import com.nightcode.snout.shared.domain.model.persons.TestPersonExceptions
import com.nightcode.snout.shared.domain.model.persons.TestPersonFlags
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.persons.GetPerson
import com.nightcode.snout.shared.domain.usecase.persons.GetPersonEvents
import com.nightcode.snout.shared.domain.usecase.persons.GetPersonExceptions
import com.nightcode.snout.shared.domain.usecase.persons.GetPersonFlags
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verify.VerifyMode.Companion.not
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@OptIn(TestData::class)
class PersonDetailViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<PersonRepository>()
        val uuid = TestPerson.uuid
        val firstPage = TestPersonEvents.take(3)
        val secondPage = TestPersonEvents.drop(3)
        val cutOff = firstPage.last().timestamp

        fun subject(): PersonDetailViewModel {
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            everySuspend { repository.getPerson(any(), uuid) } returns Either.Right(TestPerson)
            everySuspend { repository.getEvents(any(), uuid, null) } returns
                Either.Right(PersonEventPage(firstPage, nextBefore = cutOff))
            everySuspend { repository.getFlags(any(), "user-4821") } returns Either.Right(TestPersonFlags)
            everySuspend { repository.getExceptions(any(), uuid) } returns Either.Right(TestPersonExceptions)
            val projectId = GetSelectedProjectId(appStateRepository)
            return PersonDetailViewModel(
                navKey = PersonDetailNavKey(uuid),
                getPerson = GetPerson(projectId, repository),
                getEvents = GetPersonEvents(projectId, repository),
                getFlags = GetPersonFlags(projectId, repository),
                getExceptions = GetPersonExceptions(projectId, repository),
            )
        }

        should("load the person and leave the other tabs until they are shown") {
            val viewModel = subject()

            viewModel.uiState.value shouldBe PersonDetailUiState(person = TestPerson)
            verifySuspend(not) { repository.getEvents(any(), any(), any()) }
            verifySuspend(not) { repository.getFlags(any(), any()) }
        }

        should("load each tab once, the flags for the first distinct ID") {
            val viewModel = subject()

            viewModel.onTabSelected(PersonTab.Flags)
            viewModel.uiState.value.flags shouldBe TestPersonFlags
            viewModel.onTabSelected(PersonTab.Errors)
            viewModel.uiState.value.exceptions shouldBe TestPersonExceptions
            viewModel.onTabSelected(PersonTab.Flags)
            verifySuspend(exactly(1)) { repository.getFlags(TestSelectedProject.projectId, "user-4821") }
        }

        should("keep a tab's failure to that tab") {
            val viewModel = subject()
            everySuspend { repository.getFlags(any(), any()) } returns Either.Left(DataError.Forbidden)

            viewModel.onTabSelected(PersonTab.Flags)
            viewModel.uiState.value.flagsError shouldBe DataError.Forbidden
            viewModel.uiState.value.error shouldBe null
            viewModel.onTabSelected(PersonTab.Events)
            viewModel.uiState.value.events shouldBe firstPage
        }

        should("append the next page of events before the last one shown") {
            everySuspend { repository.getEvents(any(), uuid, cutOff) } returns
                Either.Right(PersonEventPage(listOf(firstPage.last()) + secondPage, nextBefore = null))
            val viewModel = subject()
            viewModel.onTabSelected(PersonTab.Events)
            viewModel.uiState.value.eventsLoadMore shouldBe LoadMoreStatus.Idle

            viewModel.onEventsEndReached()
            viewModel.uiState.value.events shouldBe TestPersonEvents
            viewModel.uiState.value.eventsLoadMore shouldBe LoadMoreStatus.Done
        }

        should("keep the events shown when the next page fails") {
            everySuspend { repository.getEvents(any(), uuid, cutOff) } returns Either.Left(DataError.RateLimited)
            val viewModel = subject()
            viewModel.onTabSelected(PersonTab.Events)

            viewModel.onEventsEndReached()
            viewModel.uiState.value.events shouldBe firstPage
            viewModel.uiState.value.eventsLoadMore shouldBe LoadMoreStatus.Failed(DataError.RateLimited)
        }

        should("refresh the person and the tab on screen, and forget the other tabs") {
            val viewModel = subject()
            viewModel.onTabSelected(PersonTab.Flags)
            viewModel.onTabSelected(PersonTab.Errors)

            viewModel.refresh()
            viewModel.uiState.value.exceptions shouldBe TestPersonExceptions
            viewModel.uiState.value.flags shouldBe null
            verifySuspend(exactly(2)) { repository.getExceptions(any(), uuid) }
            verifySuspend(exactly(2)) { repository.getPerson(any(), uuid) }
        }

        should("show a person that is gone as not found") {
            val viewModel = subject()
            everySuspend { repository.getPerson(any(), uuid) } returns Either.Left(DataError.NotFound)

            viewModel.refresh()
            viewModel.uiState.value.error shouldBe DataError.NotFound
            viewModel.uiState.value.person shouldBe TestPerson
        }
    })
