package com.nightcode.snout.features.dashboards.detail

import com.nightcode.snout.common.state.RefreshableUiState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.insights.Dashboard
import com.nightcode.snout.shared.domain.model.insights.TestDashboard

internal sealed interface TileStatus {
    data object Loading : TileStatus

    data class Failed(val error: DataError) : TileStatus
}

internal data class DashboardDetailUiState(
    val dashboard: RefreshableUiState<Dashboard> = RefreshableUiState.Loading,
    /** Status of tiles whose result is still being computed, keyed by insight id. */
    val tiles: Map<Long, TileStatus> = emptyMap(),
)

@TestData
@OptIn(TestData::class)
internal val TestDashboardDetailUiState = DashboardDetailUiState(
    dashboard = RefreshableUiState.Content(TestDashboard, isRefreshing = false),
)
