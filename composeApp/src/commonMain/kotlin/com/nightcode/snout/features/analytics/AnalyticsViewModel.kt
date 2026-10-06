package com.nightcode.snout.features.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.RefreshController
import com.nightcode.snout.common.state.cachedUiState
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.applying
import com.nightcode.snout.shared.domain.usecase.analytics.ObserveAnalytics
import com.nightcode.snout.shared.domain.usecase.analytics.RefreshAnalytics
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveAnalyticsPreferences
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
internal class AnalyticsViewModel(
    private val refreshAnalytics: RefreshAnalytics,
    observeAnalytics: ObserveAnalytics,
    observeSelectedProjectId: ObserveSelectedProjectId,
    observeAnalyticsPreferences: ObserveAnalyticsPreferences,
) : ViewModel() {

    private val preferences = observeAnalyticsPreferences()

    /** Range picked on this screen; `null` until the person taps one, so the preferred default applies. */
    private val selectedRange = MutableStateFlow<AnalyticsRange?>(null)
    private val range: Flow<AnalyticsRange> = combine(selectedRange, preferences) { selected, preferences ->
        selected ?: preferences.defaultRange
    }.distinctUntilChanged()
    private val refresh = RefreshController()

    val uiState: StateFlow<AnalyticsUiState> = combine(
        range,
        range.flatMapLatest { observeAnalytics(it) },
        preferences,
        refresh.status,
    ) { range, snapshot, preferences, status ->
        AnalyticsUiState(range = range, snapshot = cachedUiState(snapshot?.applying(preferences), status))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnalyticsUiState())

    init {
        viewModelScope.launch {
            combine(range, observeSelectedProjectId()) { range, _ -> range }
                .collectLatest { range -> refresh.run { refreshAnalytics(range) } }
        }
    }

    fun onRangeSelected(selected: AnalyticsRange) {
        selectedRange.value = selected
    }

    fun refresh() = viewModelScope.launch { refresh.run { refreshAnalytics(range.first()) } }
}
