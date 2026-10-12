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
import com.nightcode.snout.testing.subscribed
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.flowOf

@OptIn(TestData::class)
class DashboardDetailViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<DashboardRepository>()
        val pendingTile = DashboardTile.InsightTile(id = 9, insight = TestInsightSummary.copy(id = 99), result = null)
        val dashboard = TestDashboard.copy(tiles = TestDashboard.tiles + pendingTile)

        fun subject(refreshResult: Either<DataError, Unit> = Either.Right(Unit)): DashboardDetailViewModel {
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            every { appStateRepository.observeSelectedProject() } returns flowOf(TestSelectedProject)
            every { repository.observeDashboard(any(), any()) } returns flowOf(dashboard)
            everySuspend { repository.refreshDashboard(any(), any()) } returns refreshResult
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
            viewModel.uiState.subscribed {
                viewModel.uiState.value.dashboard.shouldBeInstanceOf<CachedUiState.Content<*>>()
                verifySuspend { repository.refreshTile(TestSelectedProject.projectId, dashboard.id, 99) }
                viewModel.uiState.value.tiles shouldBe emptyMap()
            }
        }

        should("keep a failed status for tiles that could not be computed") {
            everySuspend { repository.refreshTile(any(), any(), any()) } returns Either.Left(DataError.RateLimited)
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.uiState.value.tiles shouldBe mapOf(99L to TileStatus.Failed(DataError.RateLimited))
            }
        }

        should("compute a failed tile again on retry") {
            everySuspend { repository.refreshTile(any(), any(), any()) } sequentially {
                returns(Either.Left(DataError.ConnectionError))
                returns(Either.Right(Unit))
            }
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.uiState.value.tiles shouldBe mapOf(99L to TileStatus.Failed(DataError.ConnectionError))
                viewModel.retryTile(99)
                viewModel.uiState.value.tiles shouldBe emptyMap()
                verifySuspend(VerifyMode.exactly(2)) {
                    repository.refreshTile(TestSelectedProject.projectId, dashboard.id, 99)
                }
            }
        }

        should("keep the cached dashboard and report a failed refresh") {
            everySuspend { repository.refreshTile(any(), any(), any()) } returns Either.Right(Unit)
            val viewModel = subject(refreshResult = Either.Left(DataError.ConnectionError))
            viewModel.uiState.subscribed {
                viewModel.uiState.value.dashboard shouldBe CachedUiState.Content(
                    dashboard,
                    isRefreshing = false,
                    refreshError = DataError.ConnectionError,
                )
            }
        }
    })
