package com.nightcode.snout.features.logs.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.shared.domain.model.logs.LogEntry
import com.nightcode.snout.shared.domain.usecase.logs.GetLogEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class LogEntryViewModel(
    @InjectedParam private val navKey: LogEntryNavKey,
    private val getLogEntry: GetLogEntry,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CachedUiState<LogEntry>>(CachedUiState.Loading)
    val uiState: StateFlow<CachedUiState<LogEntry>> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() = viewModelScope.launch {
        _uiState.value = CachedUiState.Loading
        _uiState.value = getLogEntry(navKey.service, navKey.id, navKey.timestamp).fold(
            ifLeft = { CachedUiState.Error(it) },
            ifRight = { CachedUiState.Content(it, isRefreshing = false) },
        )
    }
}
