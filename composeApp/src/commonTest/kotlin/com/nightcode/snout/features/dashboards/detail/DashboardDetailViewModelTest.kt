package com.nightcode.snout.features.dashboards.detail

import arrow.core.Either
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.insights.DashboardRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.insights.DashboardTile
import com.nightcode.snout.shared.domain.model.insights.TestDashboard
import com.nightcode.snout.shared.domain.model.insights.TestInsightSummary
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.insights.ObserveDashboard
import com.nightcode.snout.shared.domain.usecase.insights.RefreshDashboard
import com.nightcode.snout.shared.domain.usecase.insights.RefreshDashboardTile
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(TestData::class)
class DashboardDetailViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<DashboardRepository>()
        val pendingTile = DashboardTile.InsightTile(id = 9, insight = TestInsightSummary.copy(id = 99), result = null)
        val dashboard = TestDashboard.copy(tiles = TestDashboard.tiles + pendingTile)

        fun subject(): DashboardDetailViewModel {
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            every { appStateRepository.observeSelectedProject() } returns flowOf(TestSelectedProject)
            every { repository.observeDashboard(any(), any()) } returns flowOf(dashboard)
            everySuspend { repository.refreshDashboard(any(), any()) } returns Either.Right(Unit)
            val projectId = GetSelectedProjectId(appStateRepository)
            return DashboardDetailViewModel(
                dashboardId = dashboard.id,
                refreshDashboard = RefreshDashboard(projectId, repository),
                refreshDashboardTile = RefreshDashboardTile(projectId, repository),
                observeDashboard = ObserveDashboard(ObserveSelectedProjectId(appStateRepository), repository),
            )
        }

        should("compute tiles that arrive without a result") {
            everySuspend { repository.refreshTile(any(), any(), any()) } returns Either.Right(Unit)
            val viewModel = subject()
            val collector = launch(Dispatchers.Main) { viewModel.uiState.collect {} }

            viewModel.uiState.value.dashboard.shouldBeInstanceOf<CachedUiState.Content<*>>()
            verifySuspend { repository.refreshTile(TestSelectedProject.projectId, dashboard.id, 99) }
            viewModel.uiState.value.tiles shouldBe emptyMap()
            collector.cancel()
        }

        should("keep a failed status for tiles that could not be computed") {
            everySuspend { repository.refreshTile(any(), any(), any()) } returns Either.Left(DataError.RateLimited)
            val viewModel = subject()
            val collector = launch(Dispatchers.Main) { viewModel.uiState.collect {} }

            viewModel.uiState.value.tiles shouldBe mapOf(99L to TileStatus.Failed(DataError.RateLimited))
            collector.cancel()
        }
    })
