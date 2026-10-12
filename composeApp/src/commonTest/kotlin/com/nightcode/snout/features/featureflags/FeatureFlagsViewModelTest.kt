package com.nightcode.snout.features.featureflags

import arrow.core.Either
import com.nightcode.snout.common.state.CachedUiState
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
import com.nightcode.snout.shared.domain.usecase.featureflags.ObserveFeatureFlags
import com.nightcode.snout.shared.domain.usecase.featureflags.RefreshFeatureFlags
import com.nightcode.snout.shared.domain.usecase.featureflags.SearchFeatureFlags
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
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Duration

@OptIn(TestData::class)
class FeatureFlagsViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<FeatureFlagRepository>()

        val selected = MutableStateFlow<SelectedProject?>(TestSelectedProject)

        fun subject(refreshResult: Either<DataError, Unit> = Either.Right(Unit)): FeatureFlagsViewModel {
            everySuspend { appStateRepository.getSelectedProject() } calls { selected.value }
            every { appStateRepository.observeSelectedProject() } returns selected
            every { repository.observeFeatureFlags(any()) } returns flowOf(listOf(TestFeatureFlag))
            everySuspend { repository.refreshFeatureFlags(any()) } returns refreshResult
            val projectId = GetSelectedProjectId(appStateRepository)
            return FeatureFlagsViewModel(
                refreshFeatureFlags = RefreshFeatureFlags(projectId, repository),
                searchFeatureFlags = SearchFeatureFlags(projectId, repository),
                observeFeatureFlags = ObserveFeatureFlags(ObserveSelectedProjectId(appStateRepository), repository),
                observeSelectedProjectId = ObserveSelectedProjectId(appStateRepository),
                searchDebounce = Duration.ZERO,
            )
        }

        should("list cached flags and search on query") {
            val found = TestFeatureFlag.copy(id = 2, key = "dark-mode")
            everySuspend { repository.searchFeatureFlags(any(), "dark") } returns Either.Right(listOf(found))
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.uiState.value.flags shouldBe CachedUiState.Content(
                    listOf(TestFeatureFlag),
                    isRefreshing = false,
                )
                viewModel.onQueryChanged("dark")
                viewModel.uiState.value.flags.shouldBeInstanceOf<CachedUiState.Content<*>>().data shouldBe listOf(found)
            }
        }

        should("keep the cached flags and report a failed refresh") {
            val viewModel = subject(refreshResult = Either.Left(DataError.ConnectionError))
            viewModel.uiState.subscribed {
                viewModel.uiState.value.flags shouldBe CachedUiState.Content(
                    listOf(TestFeatureFlag),
                    isRefreshing = false,
                    refreshError = DataError.ConnectionError,
                )
            }
        }

        should("go back to the cached flags when the query is cleared") {
            everySuspend { repository.searchFeatureFlags(any(), "dark") } returns Either.Right(emptyList())
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.onQueryChanged("dark")
                viewModel.uiState.value.flags shouldBe CachedUiState.Empty(isRefreshing = false)
                viewModel.onQueryChanged("")
                viewModel.uiState.value.flags shouldBe CachedUiState.Content(
                    listOf(TestFeatureFlag),
                    isRefreshing = false,
                )
            }
        }

        should("refresh again when another project is selected") {
            val viewModel = subject()
            viewModel.uiState.subscribed {
                selected.value = TestSelectedProject.copy(projectId = 2)
                verifySuspend { repository.refreshFeatureFlags(2) }
            }
        }
    })
