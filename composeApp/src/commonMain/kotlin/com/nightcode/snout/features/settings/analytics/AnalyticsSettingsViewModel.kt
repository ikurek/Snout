package com.nightcode.snout.features.settings.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPreferences
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKey
import com.nightcode.snout.shared.domain.model.analytics.WebBreakdown
import com.nightcode.snout.shared.domain.usecase.analytics.ObserveAnalyticsPreferences
import com.nightcode.snout.shared.domain.usecase.analytics.SaveAnalyticsPreferences
import com.nightcode.snout.shared.domain.usecase.telemetry.TrackAnalyticsSettingsChanged
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class AnalyticsSettingsViewModel(
    private val saveAnalyticsPreferences: SaveAnalyticsPreferences,
    private val trackAnalyticsSettingsChanged: TrackAnalyticsSettingsChanged,
    observeAnalyticsPreferences: ObserveAnalyticsPreferences,
) : ViewModel() {

    val uiState: StateFlow<AnalyticsPreferences?> = observeAnalyticsPreferences()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun onDefaultRangeSelected(range: AnalyticsRange) = update { it.copy(defaultRange = range) }

    /** The last visible stat card cannot be switched off; the screen disables it, this guards the data. */
    fun onMetricToggled(key: OverviewMetricKey, enabled: Boolean) = update { current ->
        current.withMetric(key, enabled).takeIf { it.metrics.isNotEmpty() } ?: current
    }

    fun onTrendChartToggled(enabled: Boolean) = update { it.copy(showTrendChart = enabled) }

    fun onBreakdownToggled(breakdown: WebBreakdown, enabled: Boolean) = update { it.withBreakdown(breakdown, enabled) }

    fun reset() = save(AnalyticsPreferences.Default)

    private fun update(transform: (AnalyticsPreferences) -> AnalyticsPreferences) {
        val current = uiState.value ?: return
        save(transform(current))
    }

    private fun save(preferences: AnalyticsPreferences) = viewModelScope.launch {
        saveAnalyticsPreferences(preferences)
        trackAnalyticsSettingsChanged(preferences)
    }
}
