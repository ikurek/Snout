package com.nightcode.snout.features.activitylog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.RefreshableUiState
import com.nightcode.snout.common.state.loadRefreshableList
import com.nightcode.snout.shared.domain.model.activitylog.ActivityEntry
import com.nightcode.snout.shared.domain.usecase.activitylog.GetActivityLog
import com.nightcode.snout.shared.domain.usecase.activitylog.ObserveActivityLogReloads
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class ActivityLogViewModel(
    getActivityLog: GetActivityLog,
    observeActivityLogReloads: ObserveActivityLogReloads,
) : ViewModel() {

    private val refreshes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val uiState: StateFlow<RefreshableUiState<List<ActivityEntry>>> = observeActivityLogReloads()
        .loadRefreshableList(viewModelScope, refreshes) { getActivityLog() }

    fun refresh() {
        refreshes.tryEmit(Unit)
    }
}
