package com.nightcode.snout.features.insights

import arrow.core.Either
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.insights.InsightRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.insights.TestInsightSummary
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.insights.ObserveInsights
import com.nightcode.snout.shared.domain.usecase.insights.RefreshInsights
import com.nightcode.snout.shared.domain.usecase.insights.SearchInsights
import com.nightcode.snout.shared.domain.usecase.insights.SetInsightFavorite
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlin.time.Duration

@OptIn(TestData::class)
class InsightsViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<InsightRepository>()
        val searched = TestInsightSummary.copy(id = 42, name = "Searched")

        fun subject(): InsightsViewModel {
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            every { appStateRepository.observeSelectedProject() } returns flowOf(TestSelectedProject)
            every { repository.observeInsights(any()) } returns flowOf(listOf(TestInsightSummary))
            everySuspend { repository.refreshInsights(any()) } returns Either.Right(Unit)
            val projectId = GetSelectedProjectId(appStateRepository)
            return InsightsViewModel(
                refreshInsights = RefreshInsights(projectId, repository),
                searchInsights = SearchInsights(projectId, repository),
                setInsightFavorite = SetInsightFavorite(projectId, repository),
                observeInsights = ObserveInsights(ObserveSelectedProjectId(appStateRepository), repository),
                observeSelectedProjectId = ObserveSelectedProjectId(appStateRepository),
                searchDebounce = Duration.ZERO,
            )
        }

        should("show cached insights when query is blank") {
            val viewModel = subject()
            val collector = launch(Dispatchers.Main) { viewModel.uiState.collect {} }
            viewModel.uiState.value.insights shouldBe CachedUiState.Content(
                listOf(TestInsightSummary),
                isRefreshing = false,
            )
            collector.cancel()
        }

        should("search the server when a query is typed") {
            everySuspend { repository.searchInsights(any(), "sign") } returns Either.Right(listOf(searched))
            val viewModel = subject()
            val collector = launch(Dispatchers.Main) { viewModel.uiState.collect {} }

            viewModel.onQueryChange("sign")
            viewModel.uiState.value.query shouldBe "sign"
            viewModel.uiState.value.insights.shouldBeInstanceOf<CachedUiState.Content<*>>().data shouldBe listOf(
                searched,
            )
            collector.cancel()
        }

        should("emit side effect when favoriting fails") {
            everySuspend { repository.setFavorite(any(), any(), any()) } returns Either.Left(DataError.Forbidden)
            val viewModel = subject()
            val effects = launch(Dispatchers.Main) {
                viewModel.sideEffects.first() shouldBe InsightsSideEffect.FavoriteUpdateFailed
            }
            viewModel.onFavoriteToggle(TestInsightSummary)
            effects.join()
        }
    })
