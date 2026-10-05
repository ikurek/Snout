package com.nightcode.snout.features.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.RefreshController
import com.nightcode.snout.common.state.cachedUiState
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.usecase.analytics.ObserveAnalytics
import com.nightcode.snout.shared.domain.usecase.analytics.RefreshAnalytics
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
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
) : ViewModel() {

    private val range = MutableStateFlow(AnalyticsRange.Last7Days)
    private val refresh = RefreshController()

    val uiState: StateFlow<AnalyticsUiState> = combine(
        range,
        range.flatMapLatest { observeAnalytics(it) },
        refresh.status,
    ) { range, snapshot, status ->
        AnalyticsUiState(range = range, snapshot = cachedUiState(snapshot, status))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnalyticsUiState())

    init {
        viewModelScope.launch {
            combine(range, observeSelectedProjectId()) { range, _ -> range }
                .collectLatest { range -> refresh.run { refreshAnalytics(range) } }
        }
    }

    fun onRangeSelected(selected: AnalyticsRange) {
        range.value = selected
    }

    fun refresh() = viewModelScope.launch { refresh.run { refreshAnalytics(range.value) } }
}
