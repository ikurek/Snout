package com.nightcode.snout.features.logs.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.RefreshStatus
import com.nightcode.snout.common.state.RefreshableUiState
import com.nightcode.snout.common.state.listUiState
import com.nightcode.snout.navigation.LogServiceNavKey
import com.nightcode.snout.shared.domain.model.logs.LogSeverityFilter
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
import kotlinx.coroutines.flow.map
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
    @InjectedParam private val navKey: LogServiceNavKey,
    private val getLogs: GetServiceLogs,
    private val getVolume: GetServiceLogVolume,
    searchDebounce: Duration = 300.milliseconds,
) : ViewModel() {

    private val reload = MutableStateFlow(0)
    private var cursor: String? = null
    private var loadMoreJob: Job? = null
    private var lastFilter: LogFilter? = null

    private val _uiState = MutableStateFlow(LogServiceUiState())
    val uiState: StateFlow<LogServiceUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // The screen state is the only copy of the filters; the first page follows it.
            val query = _uiState.map { it.query }
                .distinctUntilChanged()
                .debounce(searchDebounce)
                .distinctUntilChanged()
            val severity = _uiState.map { it.severity }.distinctUntilChanged()
            combine(query, severity, reload, ::LogFilter)
                .collectLatest { loadFirstPage(it) }
        }
    }

    fun onQueryChanged(text: String) {
        _uiState.update { it.copy(query = text) }
    }

    fun onSeverityChanged(value: LogSeverityFilter) {
        _uiState.update { it.copy(severity = value) }
    }

    fun refresh() {
        reload.value++
    }

    /** Called as the list nears its end; does nothing while a page loads or once the last one is in. */
    fun loadMore() {
        val state = _uiState.value
        val after = cursor
        val filter = lastFilter
        val loaded = (state.logs as? RefreshableUiState.Content)?.data ?: return
        val isLoading = state.loadMore == LoadMoreStatus.Loading || loadMoreJob?.isActive == true
        if (after == null || filter == null || isLoading) return
        loadMoreJob = viewModelScope.launch {
            _uiState.update { it.copy(loadMore = LoadMoreStatus.Loading) }
            // The filter the loaded pages were fetched with, which a query still being typed hasn't replaced yet.
            getLogs(navKey.service, navKey.range, filter.severity, filter.query, after).fold(
                ifLeft = { error -> _uiState.update { it.copy(loadMore = LoadMoreStatus.Failed(error)) } },
                ifRight = { page ->
                    cursor = page.nextCursor
                    _uiState.update {
                        it.copy(
                            logs = RefreshableUiState.Content(loaded + page.entries, isRefreshing = false),
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
            val keep = isRefresh && current is RefreshableUiState.Content
            state.copy(
                logs = if (keep) current.copy(isRefreshing = true) else RefreshableUiState.Loading,
                volume = state.volume.takeIf { isRefresh },
            )
        }
        launch {
            // The chart is a bonus: when it fails the list still stands on its own.
            val volume = getVolume(navKey.service, navKey.range, filter.severity, filter.query).getOrNull()
            _uiState.update { it.copy(volume = volume?.takeUnless { v -> v.isEmpty }) }
        }
        getLogs(navKey.service, navKey.range, filter.severity, filter.query).fold(
            ifLeft = { error -> _uiState.update { it.copy(logs = RefreshableUiState.Error(error)) } },
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
