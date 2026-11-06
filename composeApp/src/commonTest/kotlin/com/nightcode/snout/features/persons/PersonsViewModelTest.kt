package com.nightcode.snout.features.persons

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.persons.PersonRepository
import com.nightcode.snout.shared.domain.model.appstate.SelectedProject
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.persons.TestPerson
import com.nightcode.snout.shared.domain.model.persons.TestPersons
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveProjectReloads
import com.nightcode.snout.shared.domain.usecase.persons.GetPersons
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Duration

@OptIn(TestData::class)
class PersonsViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<PersonRepository>()
        val selected = MutableStateFlow<SelectedProject?>(TestSelectedProject)

        fun subject(): PersonsViewModel {
            everySuspend { appStateRepository.getSelectedProject() } calls { selected.value }
            every { appStateRepository.observeSelectedProject() } returns selected
            return PersonsViewModel(
                getPersons = GetPersons(GetSelectedProjectId(appStateRepository), repository),
                observeProjectReloads = ObserveProjectReloads(appStateRepository),
                searchDebounce = Duration.ZERO,
            )
        }

        should("load the newest persons and show search results while a query is set") {
            everySuspend { repository.getPersons(any(), "") } returns Either.Right(TestPersons)
            everySuspend { repository.getPersons(any(), "jane") } returns Either.Right(listOf(TestPerson))
            val viewModel = subject()

            viewModel.uiState.value shouldBe PersonsUiState(persons = TestPersons)
            viewModel.onQueryChanged("jane")
            viewModel.uiState.value shouldBe
                PersonsUiState(persons = TestPersons, query = "jane", searchResults = listOf(TestPerson))
            viewModel.onQueryChanged("")
            viewModel.uiState.value shouldBe PersonsUiState(persons = TestPersons)
        }

        should("show a missing scope as the error with nothing loaded") {
            everySuspend { repository.getPersons(any(), any()) } returns Either.Left(DataError.Forbidden)

            subject().uiState.value shouldBe PersonsUiState(error = DataError.Forbidden)
        }

        should("keep the persons under a banner when a refresh fails") {
            everySuspend { repository.getPersons(any(), "") } returns Either.Right(TestPersons)
            val viewModel = subject()
            everySuspend { repository.getPersons(any(), "") } returns Either.Left(DataError.ConnectionError)

            viewModel.refresh()
            viewModel.uiState.value shouldBe PersonsUiState(persons = TestPersons, error = DataError.ConnectionError)
        }

        should("refresh the search results too while a search is on screen") {
            everySuspend { repository.getPersons(any(), "") } returns Either.Right(TestPersons)
            everySuspend { repository.getPersons(any(), "jane") } returns Either.Right(listOf(TestPerson))
            val viewModel = subject()
            viewModel.onQueryChanged("jane")

            viewModel.refresh()
            verifySuspend(exactly(2)) { repository.getPersons(TestSelectedProject.projectId, "jane") }
            viewModel.uiState.value.searchResults shouldBe listOf(TestPerson)
        }

        should("refresh on entering again, but not on the first entry") {
            everySuspend { repository.getPersons(any(), "") } returns Either.Right(TestPersons)
            val viewModel = subject()

            viewModel.onScreenEntered()
            verifySuspend(exactly(1)) { repository.getPersons(any(), "") }
            viewModel.onScreenEntered()
            // Mokkery counts only calls no earlier verify matched.
            verifySuspend(exactly(1)) { repository.getPersons(any(), "") }
        }
    })
