package com.nightcode.snout.features.dashboards.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.RefreshableUiState
import com.nightcode.snout.common.state.loadRefreshable
import com.nightcode.snout.shared.domain.model.insights.Dashboard
import com.nightcode.snout.shared.domain.model.insights.DashboardTile
import com.nightcode.snout.shared.domain.model.insights.InsightResult
import com.nightcode.snout.shared.domain.usecase.insights.GetDashboard
import com.nightcode.snout.shared.domain.usecase.insights.GetDashboardTileResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
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
    private val getDashboardTileResult: GetDashboardTileResult,
    getDashboard: GetDashboard,
) : ViewModel() {

    private val refreshes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val tiles = MutableStateFlow<Map<Long, TileStatus>>(emptyMap())

    /** Results computed here for tiles PostHog returned without one, keyed by insight id; they outlive a refresh. */
    private val results = MutableStateFlow<Map<Long, InsightResult?>>(emptyMap())
    private val tileLoads = Semaphore(ParallelTileLoads)

    private val dashboard = flowOf(dashboardId).loadRefreshable(viewModelScope, refreshes) { getDashboard(it) }

    val uiState: StateFlow<DashboardDetailUiState> = combine(dashboard, results, tiles) { dashboard, results, tiles ->
        DashboardDetailUiState(dashboard = dashboard.withResults(results), tiles = tiles)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardDetailUiState())

    init {
        viewModelScope.launch {
            dashboard.collect { state -> (state as? RefreshableUiState.Content)?.let { loadMissingTiles(it.data) } }
        }
    }

    fun refresh() {
        tiles.update { current -> current.filterValues { it is TileStatus.Loading } }
        refreshes.tryEmit(Unit)
    }

    fun retryTile(insightId: Long) = loadTile(insightId)

    private fun loadMissingTiles(dashboard: Dashboard) {
        dashboard.tiles
            .filterIsInstance<DashboardTile.InsightTile>()
            .filter { it.result == null && it.insight.id !in results.value && it.insight.id !in tiles.value }
            .forEach { loadTile(it.insight.id) }
    }

    private fun loadTile(insightId: Long) {
        tiles.update { it + (insightId to TileStatus.Loading) }
        viewModelScope.launch {
            tileLoads.withPermit {
                getDashboardTileResult(dashboardId, insightId).fold(
                    ifLeft = { error -> tiles.update { it + (insightId to TileStatus.Failed(error)) } },
                    ifRight = { result ->
                        results.update { it + (insightId to result) }
                        tiles.update { it - insightId }
                    },
                )
            }
        }
    }
}

/** The dashboard with the results computed on this screen filled into the tiles PostHog sent without one. */
private fun RefreshableUiState<Dashboard>.withResults(results: Map<Long, InsightResult?>) = when (this) {
    is RefreshableUiState.Content -> copy(
        data = data.copy(
            tiles = data.tiles.map { tile ->
                if (tile is DashboardTile.InsightTile && tile.result == null) {
                    tile.copy(result = results[tile.insight.id])
                } else {
                    tile
                }
            },
        ),
    )

    else -> this
}
