package com.nightcode.snout.features.dashboards.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.model.insights.Dashboard
import com.nightcode.snout.shared.domain.model.insights.DashboardTile
import com.nightcode.snout.shared.domain.model.insights.InsightResult
import com.nightcode.snout.shared.domain.usecase.insights.GetDashboard
import com.nightcode.snout.shared.domain.usecase.insights.GetDashboardTileResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val getDashboard: GetDashboard,
    private val getDashboardTileResult: GetDashboardTileResult,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardDetailUiState())
    val uiState: StateFlow<DashboardDetailUiState> = _uiState.asStateFlow()

    /** Results computed here for tiles PostHog returned without one, keyed by insight id; they outlive a refresh. */
    private var results: Map<Long, InsightResult?> = emptyMap()
    private val tileLoads = Semaphore(ParallelTileLoads)
    private var loadJob: Job? = null
    private var hasEntered = false

    init {
        load()
    }

    /** Loads the dashboard again, and the tiles that failed. */
    fun refresh() {
        _uiState.update { state -> state.copy(tiles = state.tiles.filterValues { it is TileStatus.Loading }) }
        load()
    }

    /** Refreshes each time the screen is entered again, but not the first time nor while a load runs. */
    fun onScreenEntered() {
        if (hasEntered && loadJob?.isActive != true) refresh()
        hasEntered = true
    }

    fun retryTile(insightId: Long) = loadTile(insightId)

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = it.dashboard != null, error = null) }
            _uiState.value.dashboard?.let { loadMissingTiles(it) }
            val result = getDashboard(dashboardId)
            _uiState.update { state ->
                result.fold(
                    ifLeft = { state.copy(isRefreshing = false, error = it) },
                    ifRight = { state.copy(dashboard = it.withResults(results), isRefreshing = false, error = null) },
                )
            }
            result.onRight { loadMissingTiles(it) }
        }
    }

    private fun loadMissingTiles(dashboard: Dashboard) {
        val tiles = _uiState.value.tiles
        dashboard.tiles
            .filterIsInstance<DashboardTile.InsightTile>()
            .filter { it.result == null && it.insight.id !in results && it.insight.id !in tiles }
            .forEach { loadTile(it.insight.id) }
    }

    private fun loadTile(insightId: Long) {
        _uiState.update { it.copy(tiles = it.tiles + (insightId to TileStatus.Loading)) }
        viewModelScope.launch {
            tileLoads.withPermit {
                getDashboardTileResult(dashboardId, insightId).fold(
                    ifLeft = { error ->
                        _uiState.update { it.copy(tiles = it.tiles + (insightId to TileStatus.Failed(error))) }
                    },
                    ifRight = { result ->
                        results = results + (insightId to result)
                        _uiState.update {
                            it.copy(dashboard = it.dashboard?.withResults(results), tiles = it.tiles - insightId)
                        }
                    },
                )
            }
        }
    }
}

/** The dashboard with the results computed on this screen filled into the tiles PostHog sent without one. */
private fun Dashboard.withResults(results: Map<Long, InsightResult?>) = copy(
    tiles = tiles.map { tile ->
        if (tile is DashboardTile.InsightTile && tile.result == null) {
            tile.copy(result = results[tile.insight.id])
        } else {
            tile
        }
    },
)
