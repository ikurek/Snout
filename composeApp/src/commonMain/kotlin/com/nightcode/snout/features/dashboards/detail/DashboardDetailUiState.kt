package com.nightcode.snout.features.dashboards.detail

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.insights.Dashboard
import com.nightcode.snout.shared.domain.model.insights.TestDashboard

internal sealed interface TileStatus {
    data object Loading : TileStatus

    data class Failed(val error: DataError) : TileStatus
}

internal data class DashboardDetailUiState(
    /** The dashboard, with the results computed here filled into its tiles; null until the first load succeeds. */
    val dashboard: Dashboard? = null,
    /** Why the last load failed; a banner over [dashboard] once it's loaded. */
    val error: DataError? = null,
    val isRefreshing: Boolean = false,
    /** Status of tiles whose result is still being computed, keyed by insight id. */
    val tiles: Map<Long, TileStatus> = emptyMap(),
)

@TestData
@OptIn(TestData::class)
internal val TestDashboardDetailUiState = DashboardDetailUiState(
    dashboard = TestDashboard,
)
