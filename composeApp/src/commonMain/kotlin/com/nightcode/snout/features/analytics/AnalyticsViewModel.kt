package com.nightcode.snout.features.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.RefreshableUiState
import com.nightcode.snout.common.state.loadRefreshable
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPreferences
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsSnapshot
import com.nightcode.snout.shared.domain.model.analytics.applying
import com.nightcode.snout.shared.domain.usecase.analytics.GetAnalytics
import com.nightcode.snout.shared.domain.usecase.analytics.ObserveAnalyticsPreferences
import com.nightcode.snout.shared.domain.usecase.analytics.ObserveAnalyticsReloads
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class AnalyticsViewModel(
    getAnalytics: GetAnalytics,
    observeAnalyticsReloads: ObserveAnalyticsReloads,
    observeAnalyticsPreferences: ObserveAnalyticsPreferences,
) : ViewModel() {

    private val preferences = observeAnalyticsPreferences()

    /** Range picked on this screen; `null` until the person taps one, so the preferred default applies. */
    private val selectedRange = MutableStateFlow<AnalyticsRange?>(null)
    private val range: Flow<AnalyticsRange> = combine(selectedRange, preferences) { selected, preferences ->
        selected ?: preferences.defaultRange
    }.distinctUntilChanged()
    private val refreshes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /**
     * Reloads for the project, the range and what the preferences ask to query. A new range starts from the skeleton;
     * new preferences keep the snapshot on screen while the cards and tables they add load.
     */
    private val snapshot = combine(
        observeAnalyticsReloads(),
        range,
        preferences,
    ) { reload, range, _ -> reload to range }
        .loadRefreshable(viewModelScope, refreshes) { (_, range) -> getAnalytics(range, preferences.first()) }

    val uiState: StateFlow<AnalyticsUiState> = combine(range, snapshot, preferences) { range, snapshot, preferences ->
        AnalyticsUiState(range = range, snapshot = snapshot.applying(preferences))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnalyticsUiState())

    fun onRangeSelected(selected: AnalyticsRange) {
        selectedRange.value = selected
    }

    fun refresh() {
        refreshes.tryEmit(Unit)
    }
}

private fun RefreshableUiState<AnalyticsSnapshot>.applying(preferences: AnalyticsPreferences) = when (this) {
    is RefreshableUiState.Content -> copy(data = data.applying(preferences))
    else -> this
}
