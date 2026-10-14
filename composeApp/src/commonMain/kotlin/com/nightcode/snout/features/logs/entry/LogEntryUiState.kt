package com.nightcode.snout.features.logs.entry

import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.logs.LogEntry

internal data class LogEntryUiState(
    /** The log line; null until the first load succeeds. */
    val entry: LogEntry? = null,
    /** Why the last load failed; a banner over [entry] once it's loaded. */
    val error: DataError? = null,
    val isRefreshing: Boolean = false,
)
