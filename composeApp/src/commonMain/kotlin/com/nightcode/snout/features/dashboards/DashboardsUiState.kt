package com.nightcode.snout.features.dashboards

import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.insights.DashboardSummary

internal data class DashboardsUiState(
    /** The project's dashboards, pinned first; null until the first load succeeds. */
    val dashboards: List<DashboardSummary>? = null,
    /** Why the last load failed; a banner over [dashboards] once they're loaded. */
    val error: DataError? = null,
    val isRefreshing: Boolean = false,
)
