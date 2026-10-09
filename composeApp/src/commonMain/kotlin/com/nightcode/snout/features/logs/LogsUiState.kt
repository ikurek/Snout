package com.nightcode.snout.features.logs

import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.logs.LogService
import com.nightcode.snout.shared.domain.model.logs.LogsRange
import com.nightcode.snout.shared.domain.model.logs.TestLogServices

internal data class LogsUiState(
    val range: LogsRange = LogsRange.Last24Hours,
    val services: CachedUiState<List<LogService>> = CachedUiState.Loading,
)

@TestData
@OptIn(TestData::class)
internal val TestLogsUiState = LogsUiState(services = CachedUiState.Content(TestLogServices, isRefreshing = false))
