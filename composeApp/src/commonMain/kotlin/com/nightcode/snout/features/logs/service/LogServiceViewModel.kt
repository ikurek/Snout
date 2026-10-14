package com.nightcode.snout.features.logs.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.navigation.LogServiceNavKey
import com.nightcode.snout.shared.domain.model.logs.LogSeverityFilter
import com.nightcode.snout.shared.domain.usecase.logs.GetServiceLogVolume
import com.nightcode.snout.shared.domain.usecase.logs.GetServiceLogs
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

/** What the first page was fetched with. */
private data class LogFilter(val query: String, val severity: LogSeverityFilter)

@OptIn(FlowPreview::class)
@KoinViewModel
internal class LogServiceViewModel(
    @InjectedParam private val navKey: LogServiceNavKey,
    private val getLogs: GetServiceLogs,
    private val getVolume: GetServiceLogVolume,
    searchDebounce: Duration = 300.milliseconds,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogServiceUiState())
    val uiState: StateFlow<LogServiceUiState> = _uiState.asStateFlow()

    private var lastFilter: LogFilter? = null
    private var cursor: String? = null
    private var firstPageJob: Job? = null
    private var volumeJob: Job? = null
    private var loadMoreJob: Job? = null
    private var hasEntered = false

    init {
        viewModelScope.launch {
            // The screen state is the only copy of the filters; the first page follows the query once typing pauses
            // and the severity at once.
            val query = _uiState.map { it.query }
                .distinctUntilChanged()
                .debounce(searchDebounce)
                .distinctUntilChanged()
            val severity = _uiState.map { it.severity }.distinctUntilChanged()
            combine(query, severity, ::LogFilter).collect { loadFirstPage(it) }
        }
    }

    fun onQueryChanged(text: String) {
        _uiState.update { it.copy(query = text) }
    }

    fun onSeverityChanged(value: LogSeverityFilter) {
        _uiState.update { it.copy(severity = value) }
    }

    fun refresh() {
        lastFilter?.let { loadFirstPage(it) }
    }

    /** Refreshes each time the screen is entered again, but not the first time nor while the first page loads. */
    fun onScreenEntered() {
        if (hasEntered && firstPageJob?.isActive != true) refresh()
        hasEntered = true
    }

    /** Called as the list nears its end; does nothing while a page loads or once the last one is in. */
    fun loadMore() {
        val state = _uiState.value
        val after = cursor
        val filter = lastFilter
        val loaded = state.logs?.takeIf { it.isNotEmpty() } ?: return
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
                            logs = loaded + page.entries,
                            error = null,
                            isRefreshing = false,
                            loadMore = page.nextCursor.toLoadMoreStatus(),
                        )
                    }
                },
            )
        }
    }

    /** Lines and the volume chart load together and with the same filter, so the chart always matches the list. */
    private fun loadFirstPage(filter: LogFilter) {
        firstPageJob?.cancel()
        volumeJob?.cancel()
        loadMoreJob?.cancel()
        cursor = null
        // Pull to refresh keeps the current lines on screen; a new filter starts from the skeleton.
        val isRefresh = filter == lastFilter
        lastFilter = filter
        _uiState.update { state ->
            val kept = state.logs?.takeIf { isRefresh && it.isNotEmpty() }
            state.copy(
                logs = kept,
                error = null,
                isRefreshing = kept != null,
                volume = state.volume.takeIf { isRefresh },
            )
        }
        volumeJob = viewModelScope.launch {
            // The chart is a bonus: when it fails the list still stands on its own.
            val volume = getVolume(navKey.service, navKey.range, filter.severity, filter.query).getOrNull()
            _uiState.update { it.copy(volume = volume?.takeUnless { v -> v.isEmpty }) }
        }
        firstPageJob = viewModelScope.launch {
            // A failed refresh keeps the lines on screen under a banner, as pull to refresh does elsewhere.
            getLogs(navKey.service, navKey.range, filter.severity, filter.query).fold(
                ifLeft = { error -> _uiState.update { it.copy(isRefreshing = false, error = error) } },
                ifRight = { page ->
                    cursor = page.nextCursor
                    _uiState.update {
                        it.copy(
                            logs = page.entries,
                            error = null,
                            isRefreshing = false,
                            loadMore = page.nextCursor.toLoadMoreStatus(),
                        )
                    }
                },
            )
        }
    }

    private fun String?.toLoadMoreStatus() = if (this == null) LoadMoreStatus.Done else LoadMoreStatus.Idle
}
