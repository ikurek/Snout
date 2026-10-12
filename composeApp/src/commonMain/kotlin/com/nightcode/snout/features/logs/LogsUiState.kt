package com.nightcode.snout.features.logs

import com.nightcode.snout.common.state.RefreshableUiState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.logs.LogService
import com.nightcode.snout.shared.domain.model.logs.LogsRange
import com.nightcode.snout.shared.domain.model.logs.TestLogServices

internal data class LogsUiState(
    val range: LogsRange = LogsRange.Last24Hours,
    val services: RefreshableUiState<List<LogService>> = RefreshableUiState.Loading,
)

@TestData
@OptIn(TestData::class)
internal val TestLogsUiState = LogsUiState(services = RefreshableUiState.Content(TestLogServices, isRefreshing = false))
