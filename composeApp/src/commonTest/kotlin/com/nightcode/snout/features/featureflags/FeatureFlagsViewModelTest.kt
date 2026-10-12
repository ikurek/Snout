package com.nightcode.snout.features.featureflags

import arrow.core.Either
import com.nightcode.snout.common.state.RefreshableUiState
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
import com.nightcode.snout.testing.subscribed
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
import io.kotest.matchers.types.shouldBeInstanceOf
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
            viewModel.uiState.subscribed {
                viewModel.uiState.value.flags shouldBe
                    RefreshableUiState.Content(listOf(TestFeatureFlag), isRefreshing = false)
                verifySuspend { repository.getFeatureFlags(TestSelectedProject.projectId) }
                viewModel.onQueryChanged("dark")
                viewModel.uiState.value.flags.shouldBeInstanceOf<RefreshableUiState.Content<*>>().data shouldBe
                    listOf(found)
            }
        }

        should("go back to the loaded flags when the query is cleared") {
            everySuspend { repository.getFeatureFlags(any()) } returns Either.Right(listOf(TestFeatureFlag))
            everySuspend { repository.searchFeatureFlags(any(), "dark") } returns Either.Right(emptyList())
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.onQueryChanged("dark")
                viewModel.uiState.value.flags shouldBe RefreshableUiState.Empty(isRefreshing = false)
                viewModel.onQueryChanged("")
                viewModel.uiState.value.flags shouldBe
                    RefreshableUiState.Content(listOf(TestFeatureFlag), isRefreshing = false)
            }
        }

        should("keep the flags and report a failed pull to refresh") {
            everySuspend { repository.getFeatureFlags(any()) } sequentially {
                returns(Either.Right(listOf(TestFeatureFlag)))
                returns(Either.Left(DataError.ConnectionError))
            }
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.refresh()
                viewModel.uiState.value.flags shouldBe RefreshableUiState.Content(
                    listOf(TestFeatureFlag),
                    isRefreshing = false,
                    refreshError = DataError.ConnectionError,
                )
            }
        }

        should("load again after a flag changed in the app") {
            everySuspend { repository.getFeatureFlags(any()) } sequentially {
                returns(Either.Right(listOf(TestFeatureFlag)))
                returns(Either.Right(listOf(toggled)))
            }
            val viewModel = subject()
            viewModel.uiState.subscribed {
                changes.emit(Unit)
                viewModel.uiState.value.flags shouldBe RefreshableUiState.Content(listOf(toggled), isRefreshing = false)
                verifySuspend(exactly(2)) { repository.getFeatureFlags(TestSelectedProject.projectId) }
            }
        }

        should("load again when another project is selected") {
            everySuspend { repository.getFeatureFlags(any()) } returns Either.Right(listOf(TestFeatureFlag))
            val viewModel = subject()
            viewModel.uiState.subscribed {
                selected.value = TestSelectedProject.copy(projectId = 2)
                verifySuspend { repository.getFeatureFlags(2) }
            }
        }
    })
