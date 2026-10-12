package com.nightcode.snout.features.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.loadRefreshableList
import com.nightcode.snout.shared.domain.model.logs.LogsRange
import com.nightcode.snout.shared.domain.usecase.logs.GetLogServices
import com.nightcode.snout.shared.domain.usecase.logs.ObserveLogServiceReloads
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class LogsViewModel(getServices: GetLogServices, observeLogServiceReloads: ObserveLogServiceReloads) :
    ViewModel() {

    private val range = MutableStateFlow(LogsRange.Last24Hours)
    private val refreshes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    // Another range starts from the skeleton: the services shown so far counted another period.
    private val services = combine(observeLogServiceReloads(), range, ::Pair)
        .loadRefreshableList(viewModelScope, refreshes) { (_, range) -> getServices(range) }

    val uiState: StateFlow<LogsUiState> = combine(range, services) { range, services ->
        LogsUiState(range = range, services = services)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LogsUiState())

    fun onRangeSelected(value: LogsRange) {
        range.value = value
    }

    fun refresh() {
        refreshes.tryEmit(Unit)
    }
}
