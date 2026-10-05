package com.nightcode.snout.shared.data.analytics

import arrow.core.Either
import arrow.core.raise.either
import com.nightcode.snout.shared.api.analytics.AnalyticsApi
import com.nightcode.snout.shared.data.analytics.mapper.toBreakdownTable
import com.nightcode.snout.shared.data.analytics.mapper.toOverviewMetrics
import com.nightcode.snout.shared.data.analytics.mapper.toTimeSeries
import com.nightcode.snout.shared.data.common.JsonCacheStore
import com.nightcode.snout.shared.data.common.toDataError
import com.nightcode.snout.shared.domain.data.analytics.AnalyticsRepository
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsSnapshot
import com.nightcode.snout.shared.domain.model.analytics.WebBreakdown
import com.nightcode.snout.shared.domain.model.common.DataError
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow

private const val BreakdownRowLimit = 8

internal class AnalyticsRepositoryImpl(private val api: AnalyticsApi, private val cache: JsonCacheStore) :
    AnalyticsRepository {

    override fun observeSnapshot(projectId: Long, range: AnalyticsRange): Flow<AnalyticsSnapshot?> =
        cache.observe(cacheKey(projectId, range))

    override suspend fun refreshSnapshot(projectId: Long, range: AnalyticsRange): Either<DataError, Unit> = either {
        val snapshot = coroutineScope {
            val overview = async { api.getWebOverview(projectId, range.dateFrom) }
            val trend = async { api.getPageviewTrend(projectId, range.dateFrom, range.interval) }
            val tables = WebBreakdown.entries.map { breakdown ->
                async {
                    api.getWebStatsTable(projectId, range.dateFrom, breakdown.apiValue, BreakdownRowLimit)
                        .map { it.toBreakdownTable(breakdown) }
                }
            }
            AnalyticsSnapshot(
                range = range,
                overview = overview.await().mapLeft { it.toDataError() }.bind().toOverviewMetrics(cache.json),
                // Trend is a nice-to-have on top of the overview; a failure there must not hide the numbers.
                visitorsTrend = trend.await().getOrNull()?.toTimeSeries(cache.json),
                breakdowns = tables.map { it.await().mapLeft { error -> error.toDataError() }.bind() },
            )
        }
        cache.put(cacheKey(projectId, range), snapshot)
    }

    private fun cacheKey(projectId: Long, range: AnalyticsRange) = "analytics:$projectId:${range.name}"
}
