package com.nightcode.snout.features.dashboards

import arrow.core.Either
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.insights.DashboardRepository
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.insights.TestDashboardSummary
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.insights.ObserveDashboards
import com.nightcode.snout.shared.domain.usecase.insights.RefreshDashboards
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
class DashboardsViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<DashboardRepository>()
        val unpinned = TestDashboardSummary.copy(id = 2, name = "Alpha", pinned = false)

        fun subject(): DashboardsViewModel {
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            every { appStateRepository.observeSelectedProject() } returns flowOf(TestSelectedProject)
            every { repository.observeDashboards(any()) } returns flowOf(listOf(unpinned, TestDashboardSummary))
            everySuspend { repository.refreshDashboards(any()) } returns Either.Right(Unit)
            return DashboardsViewModel(
                refreshDashboards = RefreshDashboards(GetSelectedProjectId(appStateRepository), repository),
                observeDashboards = ObserveDashboards(ObserveSelectedProjectId(appStateRepository), repository),
                observeSelectedProjectId = ObserveSelectedProjectId(appStateRepository),
            )
        }

        should("refresh on start and list pinned dashboards first") {
            val viewModel = subject()
            val collector = launch(Dispatchers.Main) { viewModel.uiState.collect {} }

            val content = viewModel.uiState.value.shouldBeInstanceOf<CachedUiState.Content<*>>()
            content.data shouldBe listOf(TestDashboardSummary, unpinned)
            verifySuspend { repository.refreshDashboards(TestSelectedProject.projectId) }
            collector.cancel()
        }
    })
