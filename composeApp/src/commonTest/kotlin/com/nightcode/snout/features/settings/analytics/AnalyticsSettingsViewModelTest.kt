package com.nightcode.snout.features.settings.analytics

import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.telemetry.TelemetryRepository
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPreferences
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKey
import com.nightcode.snout.shared.domain.model.analytics.WebBreakdown
import com.nightcode.snout.shared.domain.usecase.analytics.ObserveAnalyticsPreferences
import com.nightcode.snout.shared.domain.usecase.analytics.SaveAnalyticsPreferences
import com.nightcode.snout.shared.domain.usecase.telemetry.TrackAnalyticsSettingsChanged
import com.nightcode.snout.testing.subscribed
import dev.mokkery.MockMode
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.not
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec
import kotlinx.coroutines.flow.flowOf

class AnalyticsSettingsViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val telemetryRepository = mock<TelemetryRepository>(MockMode.autoUnit)

        val repository = mock<AppStateRepository>(autoUnit)

        fun subject(stored: AnalyticsPreferences = AnalyticsPreferences.Default): AnalyticsSettingsViewModel {
            every { repository.observeAnalyticsPreferences() } returns flowOf(stored)
            return AnalyticsSettingsViewModel(
                saveAnalyticsPreferences = SaveAnalyticsPreferences(repository),
                trackAnalyticsSettingsChanged = TrackAnalyticsSettingsChanged(telemetryRepository),
                observeAnalyticsPreferences = ObserveAnalyticsPreferences(repository),
            )
        }

        should("save range, metric, chart and table changes") {
            val viewModel = subject()
            viewModel.uiState.subscribed {
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
                viewModel.onTrendChartToggled(false)
                verifySuspend {
                    repository.saveAnalyticsPreferences(AnalyticsPreferences.Default.copy(showTrendChart = false))
                }
                viewModel.onBreakdownToggled(WebBreakdown.Country, enabled = false)
                verifySuspend {
                    repository.saveAnalyticsPreferences(
                        AnalyticsPreferences.Default.withBreakdown(WebBreakdown.Country, enabled = false),
                    )
                }
            }
        }

        should("never switch off the last stat card") {
            val onlyVisitors = AnalyticsPreferences.Default.copy(metrics = setOf(OverviewMetricKey.Visitors))
            val viewModel = subject(onlyVisitors)
            viewModel.uiState.subscribed {
                viewModel.onMetricToggled(OverviewMetricKey.Visitors, enabled = false)
                verifySuspend { repository.saveAnalyticsPreferences(onlyVisitors) }
                verifySuspend(not) { repository.saveAnalyticsPreferences(onlyVisitors.copy(metrics = emptySet())) }
            }
        }
    })
