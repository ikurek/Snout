package com.nightcode.snout.features.analytics

import arrow.core.Either
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.analytics.AnalyticsRepository
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPreferences
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKey
import com.nightcode.snout.shared.domain.model.analytics.TestAnalyticsSnapshot
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.analytics.LoadAnalytics
import com.nightcode.snout.shared.domain.usecase.analytics.ObserveAnalyticsPreferences
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveProjectReloads
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(TestData::class)
class AnalyticsViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<AnalyticsRepository>()
        val activeUserKeys = setOf(OverviewMetricKey.WeeklyActiveUsers, OverviewMetricKey.MonthlyActiveUsers)
        val overview = Either.Right(TestAnalyticsSnapshot.overview.filter { it.key !in activeUserKeys })
        val preferences = MutableStateFlow(AnalyticsPreferences.Default)
        val selectedProject = MutableStateFlow(TestSelectedProject)

        fun subject(): AnalyticsViewModel {
            preferences.value = AnalyticsPreferences.Default
            selectedProject.value = TestSelectedProject
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            every { appStateRepository.observeSelectedProject() } returns selectedProject
            every { appStateRepository.observeAnalyticsPreferences() } returns preferences
            everySuspend { repository.getOverview(any(), any()) } returns overview
            everySuspend { repository.getActiveUsers(any(), any()) } returns
                Either.Right(TestAnalyticsSnapshot.overview.filter { it.key in activeUserKeys })
            everySuspend { repository.getTrends(any(), any()) } returns Either.Right(TestAnalyticsSnapshot.trends)
            everySuspend { repository.getBreakdown(any(), any(), any()) } returns Either.Left(DataError.NotFound)
            TestAnalyticsSnapshot.breakdowns.forEach { table ->
                everySuspend { repository.getBreakdown(any(), any(), table.breakdown) } returns Either.Right(table)
            }
            return AnalyticsViewModel(
                loadAnalytics = LoadAnalytics(GetSelectedProjectId(appStateRepository), repository),
                observeProjectReloads = ObserveProjectReloads(appStateRepository),
                observeAnalyticsPreferences = ObserveAnalyticsPreferences(appStateRepository),
            )
        }

        should("load the default range on start") {
            val viewModel = subject()
            viewModel.uiState.value.range shouldBe AnalyticsRange.Last7Days
            viewModel.uiState.value.snapshot shouldBe TestAnalyticsSnapshot
            verifySuspend { repository.getOverview(TestSelectedProject.projectId, AnalyticsRange.Last7Days) }
        }

        should("load again when the range changes") {
            val viewModel = subject()
            viewModel.onRangeSelected(AnalyticsRange.Last30Days)
            viewModel.uiState.value.range shouldBe AnalyticsRange.Last30Days
            verifySuspend { repository.getOverview(TestSelectedProject.projectId, AnalyticsRange.Last30Days) }
        }

        should("open on the preferred range and hide deselected cards and tables") {
            val viewModel = subject()
            preferences.value = AnalyticsPreferences.Default
                .copy(defaultRange = AnalyticsRange.Last30Days, showTrendChart = false)
                .withMetric(OverviewMetricKey.BounceRate, enabled = false)
            viewModel.uiState.value.range shouldBe AnalyticsRange.Last30Days
            val data = viewModel.uiState.value.snapshot.shouldNotBeNull()
            data.overview.map { it.key } shouldBe listOf(
                OverviewMetricKey.Visitors,
                OverviewMetricKey.Views,
                OverviewMetricKey.Sessions,
                OverviewMetricKey.SessionDuration,
                OverviewMetricKey.WeeklyActiveUsers,
                OverviewMetricKey.MonthlyActiveUsers,
            )
            data.trends shouldBe emptyList()
            verifySuspend { repository.getOverview(TestSelectedProject.projectId, AnalyticsRange.Last30Days) }
        }

        should("load again when the selected project changes") {
            val viewModel = subject()
            val otherProject = TestSelectedProject.copy(projectId = 42L)
            everySuspend { appStateRepository.getSelectedProject() } returns otherProject
            selectedProject.value = otherProject
            verifySuspend { repository.getOverview(42L, AnalyticsRange.Last7Days) }
        }

        should("keep the snapshot and report a failed pull to refresh") {
            val viewModel = subject()
            everySuspend { repository.getOverview(any(), any()) } returns Either.Left(DataError.ConnectionError)
            viewModel.refresh()
            viewModel.uiState.value.snapshot shouldBe TestAnalyticsSnapshot
            viewModel.uiState.value.error shouldBe DataError.ConnectionError
        }

        should("load again, keeping the snapshot, when the preferences ask for more") {
            val viewModel = subject()
            val wider = AnalyticsPreferences.Default.copy(showTrendChart = false)
            preferences.value = wider
            verifySuspend(VerifyMode.exactly(2)) {
                repository.getOverview(TestSelectedProject.projectId, AnalyticsRange.Last7Days)
            }
            viewModel.uiState.value.snapshot.shouldNotBeNull()
        }

        should("show the error placeholder when a first load's overview fails") {
            val viewModel = subject()
            everySuspend { repository.getOverview(any(), any()) } returns Either.Left(DataError.Forbidden)
            // Another range starts from nothing loaded.
            viewModel.onRangeSelected(AnalyticsRange.Last30Days)
            viewModel.uiState.value.snapshot shouldBe null
            viewModel.uiState.value.error shouldBe DataError.Forbidden
        }
    })
