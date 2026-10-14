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
import com.nightcode.snout.shared.domain.usecase.analytics.GetAnalytics
import com.nightcode.snout.shared.domain.usecase.analytics.ObserveAnalyticsPreferences
import com.nightcode.snout.shared.domain.usecase.analytics.ObserveAnalyticsReloads
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
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
        val loaded = Either.Right(TestAnalyticsSnapshot)
        val preferences = MutableStateFlow(AnalyticsPreferences.Default)
        val selectedProject = MutableStateFlow(TestSelectedProject)

        fun subject(): AnalyticsViewModel {
            preferences.value = AnalyticsPreferences.Default
            selectedProject.value = TestSelectedProject
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            every { appStateRepository.observeSelectedProject() } returns selectedProject
            every { appStateRepository.observeAnalyticsPreferences() } returns preferences
            return AnalyticsViewModel(
                getAnalytics = GetAnalytics(GetSelectedProjectId(appStateRepository), repository),
                observeAnalyticsReloads = ObserveAnalyticsReloads(ObserveSelectedProjectId(appStateRepository)),
                observeAnalyticsPreferences = ObserveAnalyticsPreferences(appStateRepository),
            )
        }

        should("load the default range on start") {
            everySuspend { repository.getSnapshot(any(), any(), any()) } returns loaded
            val viewModel = subject()
            viewModel.uiState.value.range shouldBe AnalyticsRange.Last7Days
            viewModel.uiState.value.snapshot shouldBe TestAnalyticsSnapshot
            verifySuspend {
                repository.getSnapshot(
                    TestSelectedProject.projectId,
                    AnalyticsRange.Last7Days,
                    any(),
                )
            }
        }

        should("load again when the range changes") {
            everySuspend { repository.getSnapshot(any(), any(), any()) } returns loaded
            val viewModel = subject()
            viewModel.onRangeSelected(AnalyticsRange.Last30Days)
            viewModel.uiState.value.range shouldBe AnalyticsRange.Last30Days
            verifySuspend {
                repository.getSnapshot(
                    TestSelectedProject.projectId,
                    AnalyticsRange.Last30Days,
                    any(),
                )
            }
        }

        should("open on the preferred range and hide deselected cards and tables") {
            everySuspend { repository.getSnapshot(any(), any(), any()) } returns loaded
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
            verifySuspend {
                repository.getSnapshot(
                    TestSelectedProject.projectId,
                    AnalyticsRange.Last30Days,
                    any(),
                )
            }
        }

        should("load again when the selected project changes") {
            everySuspend { repository.getSnapshot(any(), any(), any()) } returns loaded
            val viewModel = subject()
            val otherProject = TestSelectedProject.copy(projectId = 42L)
            everySuspend { appStateRepository.getSelectedProject() } returns otherProject
            selectedProject.value = otherProject
            verifySuspend { repository.getSnapshot(42L, AnalyticsRange.Last7Days, any()) }
        }

        should("keep the snapshot and report a failed pull to refresh") {
            everySuspend { repository.getSnapshot(any(), any(), any()) } sequentially {
                returns(loaded)
                returns(Either.Left(DataError.ConnectionError))
            }
            val viewModel = subject()
            viewModel.refresh()
            viewModel.uiState.value.snapshot shouldBe TestAnalyticsSnapshot
            viewModel.uiState.value.error shouldBe DataError.ConnectionError
        }

        should("load again, keeping the snapshot, when the preferences ask for more") {
            everySuspend { repository.getSnapshot(any(), any(), any()) } returns loaded
            val viewModel = subject()
            val wider = AnalyticsPreferences.Default.copy(showTrendChart = false)
            preferences.value = wider
            verifySuspend { repository.getSnapshot(TestSelectedProject.projectId, AnalyticsRange.Last7Days, wider) }
            viewModel.uiState.value.snapshot.shouldNotBeNull()
        }
    })
