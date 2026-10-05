package com.nightcode.snout.features.activitylog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.common.state.RefreshController
import com.nightcode.snout.common.state.listUiState
import com.nightcode.snout.shared.domain.model.activitylog.ActivityEntry
import com.nightcode.snout.shared.domain.usecase.activitylog.ObserveActivityLog
import com.nightcode.snout.shared.domain.usecase.activitylog.RefreshActivityLog
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class ActivityLogViewModel(
    private val refreshActivityLog: RefreshActivityLog,
    observeActivityLog: ObserveActivityLog,
    observeSelectedProjectId: ObserveSelectedProjectId,
) : ViewModel() {

    private val refresh = RefreshController()

    val uiState: StateFlow<CachedUiState<List<ActivityEntry>>> =
        combine(observeActivityLog(), refresh.status) { entries, status -> listUiState(entries, status) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CachedUiState.Loading)

    init {
        viewModelScope.launch {
            observeSelectedProjectId().collectLatest { refresh.run { refreshActivityLog() } }
        }
    }

    fun refresh() = viewModelScope.launch { refresh.run { refreshActivityLog() } }
}
