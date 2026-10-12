package com.nightcode.snout.features.dashboards

import arrow.core.Either
import com.nightcode.snout.common.state.RefreshableUiState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.insights.DashboardRepository
import com.nightcode.snout.shared.domain.model.appstate.SelectedProject
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.insights.TestDashboardSummary
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.insights.GetDashboards
import com.nightcode.snout.shared.domain.usecase.insights.ObserveDashboardReloads
import com.nightcode.snout.testing.subscribed
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
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(TestData::class)
class DashboardsViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<DashboardRepository>()
        val unpinned = TestDashboardSummary.copy(id = 2, name = "Alpha", pinned = false)
        val selected = MutableStateFlow<SelectedProject?>(TestSelectedProject)
        val loaded = Either.Right(listOf(unpinned, TestDashboardSummary))

        fun subject(): DashboardsViewModel {
            selected.value = TestSelectedProject
            everySuspend { appStateRepository.getSelectedProject() } calls { selected.value }
            every { appStateRepository.observeSelectedProject() } returns selected
            return DashboardsViewModel(
                getDashboards = GetDashboards(GetSelectedProjectId(appStateRepository), repository),
                observeDashboardReloads = ObserveDashboardReloads(ObserveSelectedProjectId(appStateRepository)),
            )
        }

        should("load on start and list pinned dashboards first") {
            everySuspend { repository.getDashboards(any()) } returns loaded
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.uiState.value shouldBe
                    RefreshableUiState.Content(listOf(TestDashboardSummary, unpinned), isRefreshing = false)
                verifySuspend { repository.getDashboards(TestSelectedProject.projectId) }
            }
        }

        should("keep the dashboards and report a failed pull to refresh") {
            everySuspend { repository.getDashboards(any()) } sequentially {
                returns(loaded)
                returns(Either.Left(DataError.ConnectionError))
            }
            val viewModel = subject()
            viewModel.uiState.subscribed {
                viewModel.refresh()
                viewModel.uiState.value shouldBe RefreshableUiState.Content(
                    listOf(TestDashboardSummary, unpinned),
                    isRefreshing = false,
                    refreshError = DataError.ConnectionError,
                )
            }
        }

        should("load again when another project is selected") {
            everySuspend { repository.getDashboards(any()) } returns loaded
            val viewModel = subject()
            viewModel.uiState.subscribed {
                selected.value = TestSelectedProject.copy(projectId = 2)
                verifySuspend { repository.getDashboards(2) }
            }
        }
    })
