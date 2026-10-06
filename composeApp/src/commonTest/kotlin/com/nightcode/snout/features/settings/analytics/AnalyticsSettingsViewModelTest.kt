package com.nightcode.snout.features.settings.analytics

import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPreferences
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKey
import com.nightcode.snout.shared.domain.model.analytics.WebBreakdown
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveAnalyticsPreferences
import com.nightcode.snout.shared.domain.usecase.appstate.SaveAnalyticsPreferences
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.not
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class AnalyticsSettingsViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val repository = mock<AppStateRepository>(autoUnit)

        fun subject(stored: AnalyticsPreferences = AnalyticsPreferences.Default): AnalyticsSettingsViewModel {
            every { repository.observeAnalyticsPreferences() } returns flowOf(stored)
            return AnalyticsSettingsViewModel(
                saveAnalyticsPreferences = SaveAnalyticsPreferences(repository),
                observeAnalyticsPreferences = ObserveAnalyticsPreferences(repository),
            )
        }

        should("save range, metric, chart and table changes") {
            val viewModel = subject()
            val collector = launch(Dispatchers.Main) { viewModel.uiState.collect {} }

            viewModel.onDefaultRangeSelected(AnalyticsRange.Today)
            verifySuspend {
                repository.saveAnalyticsPreferences(
                    AnalyticsPreferences.Default.copy(defaultRange = AnalyticsRange.Today),
                )
            }
            viewModel.onMetricToggled(OverviewMetricKey.BounceRate, enabled = false)
            verifySuspend {
                repository.saveAnalyticsPreferences(
                    AnalyticsPreferences.Default.withMetric(OverviewMetricKey.BounceRate, enabled = false),
                )
            }
            viewModel.onVisitorsTrendToggled(false)
            verifySuspend {
                repository.saveAnalyticsPreferences(AnalyticsPreferences.Default.copy(showVisitorsTrend = false))
            }
            viewModel.onBreakdownToggled(WebBreakdown.Country, enabled = false)
            verifySuspend {
                repository.saveAnalyticsPreferences(
                    AnalyticsPreferences.Default.withBreakdown(WebBreakdown.Country, enabled = false),
                )
            }
            collector.cancel()
        }

        should("never switch off the last stat card") {
            val onlyVisitors = AnalyticsPreferences.Default.copy(metrics = setOf(OverviewMetricKey.Visitors))
            val viewModel = subject(onlyVisitors)
            val collector = launch(Dispatchers.Main) { viewModel.uiState.collect {} }

            viewModel.onMetricToggled(OverviewMetricKey.Visitors, enabled = false)
            verifySuspend { repository.saveAnalyticsPreferences(onlyVisitors) }
            verifySuspend(not) { repository.saveAnalyticsPreferences(onlyVisitors.copy(metrics = emptySet())) }
            collector.cancel()
        }
    })
