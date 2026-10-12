package com.nightcode.snout.shared.data.insights

import arrow.core.Either
import arrow.core.raise.either
import com.nightcode.snout.shared.api.insights.DashboardsApi
import com.nightcode.snout.shared.api.insights.InsightsApi
import com.nightcode.snout.shared.data.common.JsonCacheStore
import com.nightcode.snout.shared.data.common.toDataError
import com.nightcode.snout.shared.data.insights.mapper.toDashboard
import com.nightcode.snout.shared.data.insights.mapper.toResult
import com.nightcode.snout.shared.data.insights.mapper.toSummary
import com.nightcode.snout.shared.domain.data.insights.DashboardRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.insights.Dashboard
import com.nightcode.snout.shared.domain.model.insights.DashboardSummary
import com.nightcode.snout.shared.domain.model.insights.DashboardTile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Single

@Single(binds = [DashboardRepository::class])
internal class DashboardRepositoryImpl(
    private val dashboardsApi: DashboardsApi,
    private val insightsApi: InsightsApi,
    private val cache: JsonCacheStore,
) : DashboardRepository {

    override fun observeDashboards(projectId: Long): Flow<List<DashboardSummary>?> = cache.observe(listKey(projectId))

    override suspend fun refreshDashboards(projectId: Long): Either<DataError, Unit> = dashboardsApi
        .getDashboards(projectId)
        .mapLeft { it.toDataError() }
        .map { response -> cache.put(listKey(projectId), response.results.map { it.toSummary() }) }

    override fun observeDashboard(projectId: Long, dashboardId: Long): Flow<Dashboard?> =
        cache.observe(detailKey(projectId, dashboardId))

    override suspend fun refreshDashboard(projectId: Long, dashboardId: Long): Either<DataError, Unit> = either {
        val fresh = dashboardsApi.getDashboard(projectId, dashboardId).mapLeft { it.toDataError() }.bind().toDashboard()
        // Keep results we already computed for tiles PostHog returned without a cached result.
        val previous = cache.observe<Dashboard>(detailKey(projectId, dashboardId)).first()
        val merged = fresh.copy(
            tiles = fresh.tiles.map { tile ->
                if (tile is DashboardTile.InsightTile && tile.result == null) {
                    val old = previous?.tiles?.firstOrNull { it.id == tile.id } as? DashboardTile.InsightTile
                    if (old?.result != null) tile.copy(result = old.result) else tile
                } else {
                    tile
                }
            },
        )
        cache.put(detailKey(projectId, dashboardId), merged)
    }

    override suspend fun refreshTile(projectId: Long, dashboardId: Long, insightId: Long): Either<DataError, Unit> =
        either {
            val insight = insightsApi.getInsight(projectId, insightId, fromDashboardId = dashboardId)
                .mapLeft { it.toDataError() }
                .bind()
            val result = insight.toResult()
            val dashboard = cache.observe<Dashboard>(detailKey(projectId, dashboardId)).first() ?: return@either
            cache.put(
                detailKey(projectId, dashboardId),
                dashboard.copy(
                    tiles = dashboard.tiles.map { tile ->
                        if (tile is DashboardTile.InsightTile && tile.insight.id == insightId) {
                            tile.copy(result = result)
                        } else {
                            tile
                        }
                    },
                ),
            )
        }

    private fun listKey(projectId: Long) = "dashboards:$projectId"

    private fun detailKey(projectId: Long, dashboardId: Long) = "dashboard:$projectId:$dashboardId"
}
