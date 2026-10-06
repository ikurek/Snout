package com.nightcode.snout.features.analytics

import arrow.core.Either
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.analytics.AnalyticsRepository
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPreferences
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsSnapshot
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKey
import com.nightcode.snout.shared.domain.model.analytics.TestAnalyticsSnapshot
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.analytics.ObserveAnalytics
import com.nightcode.snout.shared.domain.usecase.analytics.RefreshAnalytics
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveAnalyticsPreferences
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(TestData::class)
class AnalyticsViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val appStateRepository = mock<AppStateRepository>()
        val repository = mock<AnalyticsRepository>()
        val cached = MutableStateFlow(TestAnalyticsSnapshot)
        val preferences = MutableStateFlow(AnalyticsPreferences.Default)

        fun subject(): AnalyticsViewModel {
            preferences.value = AnalyticsPreferences.Default
            everySuspend { appStateRepository.getSelectedProject() } returns TestSelectedProject
            every { appStateRepository.observeSelectedProject() } returns flowOf(TestSelectedProject)
            every { appStateRepository.observeAnalyticsPreferences() } returns preferences
            every { repository.observeSnapshot(any(), any()) } returns cached
            return AnalyticsViewModel(
                refreshAnalytics = RefreshAnalytics(GetSelectedProjectId(appStateRepository), repository),
                observeAnalytics = ObserveAnalytics(ObserveSelectedProjectId(appStateRepository), repository),
                observeSelectedProjectId = ObserveSelectedProjectId(appStateRepository),
                observeAnalyticsPreferences = ObserveAnalyticsPreferences(appStateRepository),
            )
        }

        should("refresh default range on start and expose cached snapshot") {
            everySuspend { repository.refreshSnapshot(any(), any()) } returns Either.Right(Unit)
            val viewModel = subject()
            val collector = launch(Dispatchers.Main) { viewModel.uiState.collect {} }

            viewModel.uiState.value.range shouldBe AnalyticsRange.Last7Days
            viewModel.uiState.value.snapshot shouldBe CachedUiState.Content(TestAnalyticsSnapshot, isRefreshing = false)
            verifySuspend { repository.refreshSnapshot(TestSelectedProject.projectId, AnalyticsRange.Last7Days) }
            collector.cancel()
        }

        should("refresh again when range changes") {
            everySuspend { repository.refreshSnapshot(any(), any()) } returns Either.Right(Unit)
            val viewModel = subject()
            val collector = launch(Dispatchers.Main) { viewModel.uiState.collect {} }

            viewModel.onRangeSelected(AnalyticsRange.Last30Days)
            viewModel.uiState.value.range shouldBe AnalyticsRange.Last30Days
            verifySuspend { repository.refreshSnapshot(TestSelectedProject.projectId, AnalyticsRange.Last30Days) }
            collector.cancel()
        }

        should("open on the preferred range and hide deselected cards and tables") {
            everySuspend { repository.refreshSnapshot(any(), any()) } returns Either.Right(Unit)
            val viewModel = subject()
            preferences.value = AnalyticsPreferences.Default
                .copy(defaultRange = AnalyticsRange.Last30Days, showVisitorsTrend = false)
                .withMetric(OverviewMetricKey.BounceRate, enabled = false)
            val collector = launch(Dispatchers.Main) { viewModel.uiState.collect {} }

            viewModel.uiState.value.range shouldBe AnalyticsRange.Last30Days
            val snapshot = viewModel.uiState.value.snapshot.shouldBeInstanceOf<CachedUiState.Content<*>>()
            val data = snapshot.data.shouldBeInstanceOf<AnalyticsSnapshot>()
            data.overview.map { it.key } shouldBe listOf(
                OverviewMetricKey.Visitors,
                OverviewMetricKey.Views,
                OverviewMetricKey.Sessions,
                OverviewMetricKey.SessionDuration,
            )
            data.visitorsTrend shouldBe null
            verifySuspend { repository.refreshSnapshot(TestSelectedProject.projectId, AnalyticsRange.Last30Days) }
            collector.cancel()
        }

        should("keep cached content and surface refresh error") {
            everySuspend { repository.refreshSnapshot(any(), any()) } returns Either.Left(DataError.ConnectionError)
            val viewModel = subject()
            val collector = launch(Dispatchers.Main) { viewModel.uiState.collect {} }

            val snapshot = viewModel.uiState.value.snapshot.shouldBeInstanceOf<CachedUiState.Content<*>>()
            snapshot.refreshError shouldBe DataError.ConnectionError
            collector.cancel()
        }
    })
