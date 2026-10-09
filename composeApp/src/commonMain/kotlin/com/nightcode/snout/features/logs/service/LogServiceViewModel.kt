package com.nightcode.snout.features.logs.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.common.state.RefreshStatus
import com.nightcode.snout.common.state.listUiState
import com.nightcode.snout.shared.domain.model.logs.LogSeverityFilter
import com.nightcode.snout.shared.domain.model.logs.LogsRange
import com.nightcode.snout.shared.domain.usecase.logs.GetServiceLogVolume
import com.nightcode.snout.shared.domain.usecase.logs.GetServiceLogs
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/** What the first page was fetched with; bumping [reload] fetches it again. */
private data class LogFilter(val query: String, val severity: LogSeverityFilter, val reload: Int)

@OptIn(FlowPreview::class)
@KoinViewModel
internal class LogServiceViewModel(
    @InjectedParam private val service: String,
    @InjectedParam private val range: LogsRange,
    private val getLogs: GetServiceLogs,
    private val getVolume: GetServiceLogVolume,
    searchDebounce: Duration = 300.milliseconds,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val severity = MutableStateFlow(LogSeverityFilter.All)
    private val reload = MutableStateFlow(0)
    private var cursor: String? = null
    private var loadMoreJob: Job? = null
    private var lastFilter: LogFilter? = null

    private val _uiState = MutableStateFlow(LogServiceUiState())
    val uiState: StateFlow<LogServiceUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(query.debounce(searchDebounce).distinctUntilChanged(), severity, reload, ::LogFilter)
                .collectLatest { loadFirstPage(it) }
        }
    }

    fun onQueryChange(text: String) {
        query.value = text
        _uiState.update { it.copy(query = text) }
    }

    fun onSeverityChange(value: LogSeverityFilter) {
        severity.value = value
        _uiState.update { it.copy(severity = value) }
    }

    fun refresh() {
        reload.value++
    }

    /** Called as the list nears its end; does nothing while a page loads or once the last one is in. */
    fun loadMore() {
        val state = _uiState.value
        val after = cursor
        val loaded = (state.logs as? CachedUiState.Content)?.data ?: return
        if (after == null || state.loadMore == LoadMoreStatus.Loading || loadMoreJob?.isActive == true) return
        loadMoreJob = viewModelScope.launch {
            _uiState.update { it.copy(loadMore = LoadMoreStatus.Loading) }
            getLogs(service, range, state.severity, query.value, after).fold(
                ifLeft = { error -> _uiState.update { it.copy(loadMore = LoadMoreStatus.Failed(error)) } },
                ifRight = { page ->
                    cursor = page.nextCursor
                    _uiState.update {
                        it.copy(
                            logs = CachedUiState.Content(loaded + page.entries, isRefreshing = false),
                            loadMore = page.nextCursor.toLoadMoreStatus(),
                        )
                    }
                },
            )
        }
    }

    /** Lines and the volume chart load together and with the same filter, so the chart always matches the list. */
    private suspend fun loadFirstPage(filter: LogFilter) = coroutineScope {
        loadMoreJob?.cancel()
        cursor = null
        // Pull to refresh keeps the current lines on screen; a new filter starts from the skeleton.
        val isRefresh = lastFilter?.copy(reload = filter.reload) == filter
        lastFilter = filter
        _uiState.update { state ->
            val current = state.logs
            val keep = isRefresh && current is CachedUiState.Content
            state.copy(
                logs = if (keep) current.copy(isRefreshing = true) else CachedUiState.Loading,
                volume = state.volume.takeIf { isRefresh },
            )
        }
        launch {
            // The chart is a bonus: when it fails the list still stands on its own.
            val volume = getVolume(service, range, filter.severity, filter.query).getOrNull()
            _uiState.update { it.copy(volume = volume?.takeUnless { v -> v.isEmpty }) }
        }
        getLogs(service, range, filter.severity, filter.query).fold(
            ifLeft = { error -> _uiState.update { it.copy(logs = CachedUiState.Error(error)) } },
            ifRight = { page ->
                cursor = page.nextCursor
                _uiState.update {
                    it.copy(
                        logs = listUiState(page.entries, RefreshStatus.Idle),
                        loadMore = page.nextCursor.toLoadMoreStatus(),
                    )
                }
            },
        )
    }

    private fun String?.toLoadMoreStatus() = if (this == null) LoadMoreStatus.Done else LoadMoreStatus.Idle
}
