package com.nightcode.snout.features.logs.service

import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.logs.LogEntry
import com.nightcode.snout.shared.domain.model.logs.LogSeverityFilter
import com.nightcode.snout.shared.domain.model.logs.LogVolume
import com.nightcode.snout.shared.domain.model.logs.TestLogEntries
import com.nightcode.snout.shared.domain.model.logs.TestLogVolume

/** Loading the page after the ones on screen. */
internal sealed interface LoadMoreStatus {
    /** More pages exist and none is loading. */
    data object Idle : LoadMoreStatus

    data object Loading : LoadMoreStatus

    data class Failed(val error: DataError) : LoadMoreStatus

    /** The last page is on screen. */
    data object Done : LoadMoreStatus
}

internal data class LogServiceUiState(
    val query: String = "",
    val severity: LogSeverityFilter = LogSeverityFilter.All,
    val logs: CachedUiState<List<LogEntry>> = CachedUiState.Loading,
    val loadMore: LoadMoreStatus = LoadMoreStatus.Done,
    /** Volume per level for the same filter; null while loading, when empty or when it failed. */
    val volume: LogVolume? = null,
)

@TestData
@OptIn(TestData::class)
internal val TestLogServiceUiState = LogServiceUiState(
    logs = CachedUiState.Content(TestLogEntries, isRefreshing = false),
    loadMore = LoadMoreStatus.Idle,
    volume = TestLogVolume,
)
