package com.nightcode.snout.features.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.RefreshController
import com.nightcode.snout.common.state.listUiState
import com.nightcode.snout.shared.domain.model.logs.LogsRange
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.logs.ObserveLogServices
import com.nightcode.snout.shared.domain.usecase.logs.RefreshLogServices
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
internal class LogsViewModel(
    private val refreshServices: RefreshLogServices,
    observeServices: ObserveLogServices,
    observeSelectedProjectId: ObserveSelectedProjectId,
) : ViewModel() {

    private val range = MutableStateFlow(LogsRange.Last24Hours)
    private val refresh = RefreshController()

    // Each range has its own cache entry, so switching shows the last known services while they refresh.
    val uiState: StateFlow<LogsUiState> = combine(
        range,
        range.flatMapLatest { observeServices(it) },
        refresh.status,
    ) { range, services, status -> LogsUiState(range = range, services = listUiState(services, status)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LogsUiState())

    init {
        viewModelScope.launch {
            combine(observeSelectedProjectId(), range) { _, range -> range }
                .collectLatest { range -> refresh.run { refreshServices(range) } }
        }
    }

    fun onRangeChange(value: LogsRange) {
        range.value = value
    }

    fun refresh() = viewModelScope.launch { refresh.run { refreshServices(range.value) } }
}
