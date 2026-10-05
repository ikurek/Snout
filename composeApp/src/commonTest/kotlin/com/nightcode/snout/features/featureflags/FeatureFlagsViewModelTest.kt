package com.nightcode.snout.features.featureflags

import arrow.core.Either
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.featureflags.FeatureFlagRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.featureflags.TestFeatureFlag
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.featureflags.ObserveFeatureFlags
import com.nightcode.snout.shared.domain.usecase.featureflags.RefreshFeatureFlags
import com.nightcode.snout.shared.domain.usecase.featureflags.SearchFeatureFlags
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlin.time.Duration

@OptIn(TestData::class)
class FeatureFlagsViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<FeatureFlagRepository>()

        fun subject(): FeatureFlagsViewModel {
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            every { appStateRepository.observeSelectedProject() } returns flowOf(TestSelectedProject)
            every { repository.observeFlags(any()) } returns flowOf(listOf(TestFeatureFlag))
            everySuspend { repository.refreshFlags(any()) } returns Either.Right(Unit)
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
            everySuspend { repository.searchFlags(any(), "dark") } returns Either.Right(listOf(found))
            val viewModel = subject()
            val collector = launch(Dispatchers.Main) { viewModel.uiState.collect {} }

            viewModel.uiState.value.flags shouldBe CachedUiState.Content(listOf(TestFeatureFlag), isRefreshing = false)
            viewModel.onQueryChange("dark")
            viewModel.uiState.value.flags.shouldBeInstanceOf<CachedUiState.Content<*>>().data shouldBe listOf(found)
            collector.cancel()
        }
    })
