package com.nightcode.snout.features.insights

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.insights.InsightRepository
import com.nightcode.snout.shared.domain.model.appstate.SelectedProject
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.insights.TestInsightSummary
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.insights.GetInsights
import com.nightcode.snout.shared.domain.usecase.insights.ObserveInsightReloads
import com.nightcode.snout.shared.domain.usecase.insights.SearchInsights
import com.nightcode.snout.shared.domain.usecase.insights.SetInsightFavorite
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration

@OptIn(TestData::class)
class InsightsViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<InsightRepository>()
        val selected = MutableStateFlow<SelectedProject?>(TestSelectedProject)
        val changes = MutableSharedFlow<Unit>()
        val searched = TestInsightSummary.copy(id = 42, name = "Searched")
        val favorited = TestInsightSummary.copy(favorited = true)

        fun subject(): InsightsViewModel {
            selected.value = TestSelectedProject
            everySuspend { appStateRepository.getSelectedProject() } calls { selected.value }
            every { appStateRepository.observeSelectedProject() } returns selected
            every { repository.changes } returns changes
            val projectId = GetSelectedProjectId(appStateRepository)
            return InsightsViewModel(
                searchInsights = SearchInsights(projectId, repository),
                setInsightFavorite = SetInsightFavorite(projectId, repository),
                getInsights = GetInsights(projectId, repository),
                observeInsightReloads = ObserveInsightReloads(ObserveSelectedProjectId(appStateRepository), repository),
                searchDebounce = Duration.ZERO,
            )
        }

        should("load the insights on start while the query is blank") {
            everySuspend { repository.getInsights(any()) } returns Either.Right(listOf(TestInsightSummary))
            val viewModel = subject()
            viewModel.uiState.value shouldBe InsightsUiState(insights = listOf(TestInsightSummary))
            verifySuspend { repository.getInsights(TestSelectedProject.projectId) }
        }

        should("search the server when a query is typed") {
            everySuspend { repository.getInsights(any()) } returns Either.Right(listOf(TestInsightSummary))
            everySuspend { repository.searchInsights(any(), "sign") } returns Either.Right(listOf(searched))
            val viewModel = subject()
            viewModel.onQueryChanged("sign")
            viewModel.uiState.value.query shouldBe "sign"
            viewModel.uiState.value.searchResults shouldBe listOf(searched)
        }

        should("keep the insights and report a failed pull to refresh") {
            everySuspend { repository.getInsights(any()) } sequentially {
                returns(Either.Right(listOf(TestInsightSummary)))
                returns(Either.Left(DataError.ConnectionError))
            }
            val viewModel = subject()
            viewModel.refresh()
            viewModel.uiState.value shouldBe InsightsUiState(
                insights = listOf(TestInsightSummary),
                error = DataError.ConnectionError,
            )
        }

        should("load again after an insight changed in the app") {
            everySuspend { repository.getInsights(any()) } sequentially {
                returns(Either.Right(listOf(TestInsightSummary)))
                returns(Either.Right(listOf(favorited)))
            }
            val viewModel = subject()
            changes.emit(Unit)
            viewModel.uiState.value shouldBe InsightsUiState(insights = listOf(favorited))
        }

        should("load again when another project is selected") {
            everySuspend { repository.getInsights(any()) } returns Either.Right(listOf(TestInsightSummary))
            subject()
            selected.value = TestSelectedProject.copy(projectId = 2)
            verifySuspend { repository.getInsights(2) }
        }

        should("emit side effect when favoriting fails") {
            everySuspend { repository.getInsights(any()) } returns Either.Right(listOf(TestInsightSummary))
            everySuspend { repository.setFavorite(any(), any(), any()) } returns Either.Left(DataError.Forbidden)
            val viewModel = subject()
            val effects = launch(Dispatchers.Main) {
                viewModel.sideEffects.first() shouldBe InsightsSideEffect.FavoriteUpdateFailed
            }
            viewModel.toggleFavorite(TestInsightSummary)
            effects.join()
        }
    })
