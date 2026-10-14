package com.nightcode.snout.features.activitylog

import com.nightcode.snout.shared.domain.model.activitylog.ActivityEntry
import com.nightcode.snout.shared.domain.model.common.DataError

internal data class ActivityLogUiState(
    /** The project's activity, newest first; null until the first load succeeds. */
    val entries: List<ActivityEntry>? = null,
    /** Why the last load failed; a banner over [entries] once they're loaded. */
    val error: DataError? = null,
    val isRefreshing: Boolean = false,
)
