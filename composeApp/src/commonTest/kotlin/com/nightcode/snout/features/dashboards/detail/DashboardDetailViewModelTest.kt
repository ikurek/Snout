package com.nightcode.snout.features.dashboards.detail

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.insights.DashboardRepository
import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.insights.Dashboard
import com.nightcode.snout.shared.domain.model.insights.DashboardTile
import com.nightcode.snout.shared.domain.model.insights.TestDashboard
import com.nightcode.snout.shared.domain.model.insights.TestInsightSummary
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryEvent
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.insights.GetDashboard
import com.nightcode.snout.shared.domain.usecase.insights.GetDashboardTileResult
import com.nightcode.snout.shared.domain.usecase.telemetry.TrackDashboardViewed
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.resetCalls
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

@OptIn(TestData::class)
class DashboardDetailViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val telemetryRepository = mock<TelemetryRepository>(MockMode.autoUnit)
        beforeTest { resetCalls(telemetryRepository) }

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<DashboardRepository>()
        val pendingTile = DashboardTile.InsightTile(id = 9, insight = TestInsightSummary.copy(id = 99), result = null)
        val dashboard = TestDashboard.copy(tiles = TestDashboard.tiles + pendingTile)
        val computed = TestDashboard.tiles.filterIsInstance<DashboardTile.InsightTile>().first {
            it.result != null
        }.result

        fun subject(): DashboardDetailViewModel {
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            val projectId = GetSelectedProjectId(appStateRepository)
            return DashboardDetailViewModel(
                dashboardId = dashboard.id,
                getDashboardTileResult = GetDashboardTileResult(projectId, repository),
                getDashboard = GetDashboard(projectId, repository),
                trackDashboardViewed = TrackDashboardViewed(telemetryRepository),
            )
        }

        fun DashboardDetailViewModel.shownDashboard(): Dashboard = uiState.value.dashboard.shouldNotBeNull()

        fun Dashboard.pendingResult() = tiles.single { it.id == pendingTile.id }
            .shouldBeInstanceOf<DashboardTile.InsightTile>()
            .result

        should("load the dashboard and fill in tiles that arrive without a result") {
            everySuspend { repository.getDashboard(any(), any()) } returns Either.Right(dashboard)
            everySuspend { repository.getTileResult(any(), any(), any()) } returns Either.Right(computed)
            val viewModel = subject()
            verifySuspend { repository.getTileResult(TestSelectedProject.projectId, dashboard.id, 99) }
            viewModel.shownDashboard().pendingResult() shouldBe computed
            viewModel.uiState.value.tiles shouldBe emptyMap()
        }

        should("report the view once per visit, not on each refresh") {
            everySuspend { repository.getDashboard(any(), any()) } returns Either.Right(dashboard)
            everySuspend { repository.getTileResult(any(), any(), any()) } returns Either.Right(computed)
            val viewModel = subject()
            viewModel.refresh()
            verify(VerifyMode.exactly(1)) {
                telemetryRepository.track(TelemetryEvent.DashboardViewed(tileCount = dashboard.tiles.size))
            }
        }

        should("keep a failed status for tiles that could not be computed") {
            everySuspend { repository.getDashboard(any(), any()) } returns Either.Right(dashboard)
            everySuspend { repository.getTileResult(any(), any(), any()) } returns Either.Left(DataError.RateLimited)
            val viewModel = subject()
            viewModel.uiState.value.tiles shouldBe mapOf(99L to TileStatus.Failed(DataError.RateLimited))
        }

        should("compute a failed tile again on retry without loading the dashboard again") {
            everySuspend { repository.getDashboard(any(), any()) } returns Either.Right(dashboard)
            everySuspend { repository.getTileResult(any(), any(), any()) } sequentially {
                returns(Either.Left(DataError.ConnectionError))
                returns(Either.Right(computed))
            }
            val viewModel = subject()
            viewModel.uiState.value.tiles shouldBe mapOf(99L to TileStatus.Failed(DataError.ConnectionError))
            viewModel.retryTile(99)
            viewModel.uiState.value.tiles shouldBe emptyMap()
            viewModel.shownDashboard().pendingResult() shouldBe computed
            verifySuspend(exactly(2)) { repository.getTileResult(TestSelectedProject.projectId, dashboard.id, 99) }
            verifySuspend(exactly(1)) { repository.getDashboard(TestSelectedProject.projectId, dashboard.id) }
        }

        should("keep the dashboard and its computed tiles and report a failed pull to refresh") {
            everySuspend { repository.getDashboard(any(), any()) } sequentially {
                returns(Either.Right(dashboard))
                returns(Either.Left(DataError.ConnectionError))
            }
            everySuspend { repository.getTileResult(any(), any(), any()) } returns Either.Right(computed)
            val viewModel = subject()
            viewModel.refresh()
            viewModel.uiState.value.error shouldBe DataError.ConnectionError
            viewModel.shownDashboard().pendingResult() shouldBe computed
        }

        should("compute the failed tiles again on pull to refresh") {
            everySuspend { repository.getDashboard(any(), any()) } returns Either.Right(dashboard)
            everySuspend { repository.getTileResult(any(), any(), any()) } sequentially {
                returns(Either.Left(DataError.ConnectionError))
                returns(Either.Right(computed))
            }
            val viewModel = subject()
            viewModel.uiState.value.tiles shouldBe mapOf(99L to TileStatus.Failed(DataError.ConnectionError))
            viewModel.refresh()
            viewModel.uiState.value.tiles shouldBe emptyMap()
            viewModel.shownDashboard().pendingResult() shouldBe computed
            verifySuspend(exactly(2)) { repository.getDashboard(TestSelectedProject.projectId, dashboard.id) }
        }
    })
