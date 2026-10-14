package com.nightcode.snout.features.featureflags

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.featureflags.FeatureFlagRepository
import com.nightcode.snout.shared.domain.model.appstate.SelectedProject
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.featureflags.TestFeatureFlag
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.featureflags.GetFeatureFlags
import com.nightcode.snout.shared.domain.usecase.featureflags.ObserveFeatureFlagReloads
import com.nightcode.snout.shared.domain.usecase.featureflags.SearchFeatureFlags
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Duration

@OptIn(TestData::class)
class FeatureFlagsViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<FeatureFlagRepository>()
        val selected = MutableStateFlow<SelectedProject?>(TestSelectedProject)
        val changes = MutableSharedFlow<Unit>()
        val toggled = TestFeatureFlag.copy(active = false)

        fun subject(): FeatureFlagsViewModel {
            selected.value = TestSelectedProject
            everySuspend { appStateRepository.getSelectedProject() } calls { selected.value }
            every { appStateRepository.observeSelectedProject() } returns selected
            every { repository.changes } returns changes
            val projectId = GetSelectedProjectId(appStateRepository)
            return FeatureFlagsViewModel(
                searchFeatureFlags = SearchFeatureFlags(projectId, repository),
                getFeatureFlags = GetFeatureFlags(projectId, repository),
                observeFeatureFlagReloads = ObserveFeatureFlagReloads(
                    ObserveSelectedProjectId(appStateRepository),
                    repository,
                ),
                searchDebounce = Duration.ZERO,
            )
        }

        should("load the flags on start and search on query") {
            everySuspend { repository.getFeatureFlags(any()) } returns Either.Right(listOf(TestFeatureFlag))
            val found = TestFeatureFlag.copy(id = 2, key = "dark-mode")
            everySuspend { repository.searchFeatureFlags(any(), "dark") } returns Either.Right(listOf(found))
            val viewModel = subject()
            viewModel.uiState.value shouldBe FeatureFlagsUiState(flags = listOf(TestFeatureFlag))
            verifySuspend { repository.getFeatureFlags(TestSelectedProject.projectId) }
            viewModel.onQueryChanged("dark")
            viewModel.uiState.value shouldBe FeatureFlagsUiState(
                flags = listOf(TestFeatureFlag),
                query = "dark",
                searchResults = listOf(found),
            )
        }

        should("go back to the loaded flags when the query is cleared") {
            everySuspend { repository.getFeatureFlags(any()) } returns Either.Right(listOf(TestFeatureFlag))
            everySuspend { repository.searchFeatureFlags(any(), "dark") } returns Either.Right(emptyList())
            val viewModel = subject()
            viewModel.onQueryChanged("dark")
            viewModel.uiState.value.searchResults shouldBe emptyList()
            viewModel.onQueryChanged("")
            viewModel.uiState.value shouldBe FeatureFlagsUiState(flags = listOf(TestFeatureFlag))
        }

        should("keep the flags and report a failed pull to refresh") {
            everySuspend { repository.getFeatureFlags(any()) } sequentially {
                returns(Either.Right(listOf(TestFeatureFlag)))
                returns(Either.Left(DataError.ConnectionError))
            }
            val viewModel = subject()
            viewModel.refresh()
            viewModel.uiState.value shouldBe FeatureFlagsUiState(
                flags = listOf(TestFeatureFlag),
                error = DataError.ConnectionError,
            )
        }

        should("load again after a flag changed in the app") {
            everySuspend { repository.getFeatureFlags(any()) } sequentially {
                returns(Either.Right(listOf(TestFeatureFlag)))
                returns(Either.Right(listOf(toggled)))
            }
            val viewModel = subject()
            changes.emit(Unit)
            viewModel.uiState.value shouldBe FeatureFlagsUiState(flags = listOf(toggled))
            verifySuspend(exactly(2)) { repository.getFeatureFlags(TestSelectedProject.projectId) }
        }

        should("load again when another project is selected") {
            everySuspend { repository.getFeatureFlags(any()) } returns Either.Right(listOf(TestFeatureFlag))
            subject()
            selected.value = TestSelectedProject.copy(projectId = 2)
            verifySuspend { repository.getFeatureFlags(2) }
        }

        should("report a failed search, and search again once the query changes") {
            everySuspend { repository.getFeatureFlags(any()) } returns Either.Right(listOf(TestFeatureFlag))
            everySuspend { repository.searchFeatureFlags(any(), "dark") } returns
                Either.Left(DataError.ConnectionError)
            everySuspend { repository.searchFeatureFlags(any(), "dar") } returns Either.Right(listOf(TestFeatureFlag))
            val viewModel = subject()

            viewModel.onQueryChanged("dark")
            viewModel.uiState.value.searchError shouldBe DataError.ConnectionError
            viewModel.onQueryChanged("dar")
            viewModel.uiState.value.searchError shouldBe null
            viewModel.uiState.value.searchResults shouldBe listOf(TestFeatureFlag)
        }
    })
