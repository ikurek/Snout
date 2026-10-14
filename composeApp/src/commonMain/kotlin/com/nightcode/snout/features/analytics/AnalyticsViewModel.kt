package com.nightcode.snout.features.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPreferences
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsSnapshot
import com.nightcode.snout.shared.domain.model.analytics.applying
import com.nightcode.snout.shared.domain.usecase.analytics.GetAnalytics
import com.nightcode.snout.shared.domain.usecase.analytics.ObserveAnalyticsPreferences
import com.nightcode.snout.shared.domain.usecase.analytics.ObserveAnalyticsReloads
import com.nightcode.snout.shared.domain.usecase.appstate.ProjectReload
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class AnalyticsViewModel(
    private val getAnalytics: GetAnalytics,
    observeAnalyticsReloads: ObserveAnalyticsReloads,
    observeAnalyticsPreferences: ObserveAnalyticsPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    /** Range picked on this screen; `null` until the person taps one, so the preferred default applies. */
    private val selectedRange = MutableStateFlow<AnalyticsRange?>(null)

    /** The project and range the snapshot on screen is for. */
    private var loadedFor: Pair<ProjectReload, AnalyticsRange>? = null

    /** The snapshot as fetched; the screen shows it with the current preferences applied. */
    private var fetched: AnalyticsSnapshot? = null
    private var currentPreferences: AnalyticsPreferences? = null
    private var loadJob: Job? = null
    private var hasEntered = false

    init {
        val preferences = observeAnalyticsPreferences()
        val range = combine(selectedRange, preferences) { selected, preferences ->
            selected ?: preferences.defaultRange
        }.distinctUntilChanged()
        viewModelScope.launch {
            // Reloads for the project, the range and what the preferences ask to query. A new range starts from the
            // skeleton; new preferences keep the snapshot on screen while the cards and tables they add load.
            combine(observeAnalyticsReloads(), range, preferences, ::Triple).collect { (reload, range, preferences) ->
                currentPreferences = preferences
                if (reload to range != loadedFor) fetched = null
                loadedFor = reload to range
                _uiState.update { it.copy(range = range, snapshot = fetched?.applying(preferences)) }
                load()
            }
        }
    }

    fun onRangeSelected(selected: AnalyticsRange) {
        selectedRange.value = selected
    }

    fun refresh() = load()

    /** Refreshes each time the screen is entered again, but not the first time nor while a load runs. */
    fun onScreenEntered() {
        if (hasEntered && loadJob?.isActive != true) refresh()
        hasEntered = true
    }

    private fun load() {
        val (_, range) = loadedFor ?: return
        val preferences = currentPreferences ?: return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = it.snapshot != null, error = null) }
            val result = getAnalytics(range, preferences)
            result.onRight { fetched = it }
            _uiState.update { state ->
                result.fold(
                    ifLeft = { state.copy(isRefreshing = false, error = it) },
                    ifRight = { state.copy(snapshot = it.applying(preferences), isRefreshing = false, error = null) },
                )
            }
        }
    }
}
