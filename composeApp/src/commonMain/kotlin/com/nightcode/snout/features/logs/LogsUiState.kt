package com.nightcode.snout.features.logs

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.logs.LogService
import com.nightcode.snout.shared.domain.model.logs.LogsRange
import com.nightcode.snout.shared.domain.model.logs.TestLogServices

internal data class LogsUiState(
    val range: LogsRange = LogsRange.Last24Hours,
    /** Services that logged in [range]; null until loaded for it. */
    val services: List<LogService>? = null,
    /** Why the last load failed; a banner over [services] once they're loaded. */
    val error: DataError? = null,
    val isRefreshing: Boolean = false,
)

@TestData
@OptIn(TestData::class)
internal val TestLogsUiState = LogsUiState(services = TestLogServices)
