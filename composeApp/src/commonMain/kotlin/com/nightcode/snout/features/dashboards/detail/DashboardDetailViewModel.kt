package com.nightcode.snout.features.dashboards.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.RefreshController
import com.nightcode.snout.common.state.cachedUiState
import com.nightcode.snout.shared.domain.model.insights.Dashboard
import com.nightcode.snout.shared.domain.model.insights.DashboardTile
import com.nightcode.snout.shared.domain.usecase.insights.ObserveDashboard
import com.nightcode.snout.shared.domain.usecase.insights.RefreshDashboard
import com.nightcode.snout.shared.domain.usecase.insights.RefreshDashboardTile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

private const val ParallelTileLoads = 3

@KoinViewModel
internal class DashboardDetailViewModel(
    @InjectedParam private val dashboardId: Long,
    private val refreshDashboard: RefreshDashboard,
    private val refreshDashboardTile: RefreshDashboardTile,
    observeDashboard: ObserveDashboard,
) : ViewModel() {

    private val refresh = RefreshController()
    private val tiles = MutableStateFlow<Map<Long, TileStatus>>(emptyMap())
    private val tileLoads = Semaphore(ParallelTileLoads)

    val uiState: StateFlow<DashboardDetailUiState> = combine(
        observeDashboard(dashboardId).onEach { dashboard -> dashboard?.let(::loadMissingTiles) },
        refresh.status,
        tiles,
    ) { dashboard, status, tiles ->
        DashboardDetailUiState(dashboard = cachedUiState(dashboard, status), tiles = tiles)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardDetailUiState())

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        tiles.update { current -> current.filterValues { it is TileStatus.Loading } }
        refresh.run { refreshDashboard(dashboardId) }
    }

    fun retryTile(insightId: Long) = loadTile(insightId)

    private fun loadMissingTiles(dashboard: Dashboard) {
        dashboard.tiles
            .filterIsInstance<DashboardTile.InsightTile>()
            .filter { it.result == null && it.insight.id !in tiles.value }
            .forEach { loadTile(it.insight.id) }
    }

    private fun loadTile(insightId: Long) {
        tiles.update { it + (insightId to TileStatus.Loading) }
        viewModelScope.launch {
            tileLoads.withPermit {
                refreshDashboardTile(dashboardId, insightId).fold(
                    ifLeft = { error -> tiles.update { it + (insightId to TileStatus.Failed(error)) } },
                    ifRight = { tiles.update { it - insightId } },
                )
            }
        }
    }
}
