package com.nightcode.snout.shared.data.analytics

import arrow.core.Either
import arrow.core.raise.either
import com.nightcode.snout.shared.api.analytics.AnalyticsApi
import com.nightcode.snout.shared.data.analytics.mapper.toActiveUserMetrics
import com.nightcode.snout.shared.data.analytics.mapper.toBreakdownTable
import com.nightcode.snout.shared.data.analytics.mapper.toMetricTrends
import com.nightcode.snout.shared.data.analytics.mapper.toOverviewMetrics
import com.nightcode.snout.shared.data.analytics.mapper.toPropertyBreakdownTable
import com.nightcode.snout.shared.data.common.JsonCacheStore
import com.nightcode.snout.shared.data.common.toDataError
import com.nightcode.snout.shared.domain.data.analytics.AnalyticsRepository
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPreferences
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsSnapshot
import com.nightcode.snout.shared.domain.model.analytics.BreakdownSource
import com.nightcode.snout.shared.domain.model.analytics.BreakdownTable
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKey
import com.nightcode.snout.shared.domain.model.analytics.WebBreakdown
import com.nightcode.snout.shared.domain.model.common.DataError
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

private const val BreakdownRowLimit = 8

/** Bumped whenever the cached snapshot shape changes so stale payloads are ignored rather than mis-decoded. */
private const val CacheVersion = "v2"

private val ActiveUserKeys = setOf(OverviewMetricKey.WeeklyActiveUsers, OverviewMetricKey.MonthlyActiveUsers)

@Single(binds = [AnalyticsRepository::class])
internal class AnalyticsRepositoryImpl(private val api: AnalyticsApi, private val cache: JsonCacheStore) :
    AnalyticsRepository {

    override fun observeSnapshot(projectId: Long, range: AnalyticsRange): Flow<AnalyticsSnapshot?> =
        cache.observe(cacheKey(projectId, range))

    /**
     * The overview is the one request that must succeed; everything else is optional on top of it and is simply
     * left out when PostHog refuses or fails it, so one unsupported breakdown never blanks the whole tab.
     */
    override suspend fun refreshSnapshot(
        projectId: Long,
        range: AnalyticsRange,
        preferences: AnalyticsPreferences,
    ): Either<DataError, Unit> = either {
        val snapshot = coroutineScope {
            val overview = async { api.getWebOverview(projectId, range.dateFrom) }
            val activeUsers = if (preferences.metrics.any { it in ActiveUserKeys }) {
                async { api.getActiveUsers(projectId, range.dateFrom) }
            } else {
                null
            }
            val trends = if (preferences.showTrendChart) {
                async { api.getMetricTrends(projectId, range.dateFrom, range.interval) }
            } else {
                null
            }
            val tables = WebBreakdown.entries
                .filter { it in preferences.breakdowns }
                .map { breakdown -> async { fetchTable(projectId, range, breakdown) } }

            val metrics = overview.await().mapLeft { it.toDataError() }.bind().toOverviewMetrics(cache.json)
            AnalyticsSnapshot(
                range = range,
                overview = metrics + activeUsers?.await()?.getOrNull()?.toActiveUserMetrics(cache.json).orEmpty(),
                trends = trends?.await()?.getOrNull()?.toMetricTrends(cache.json).orEmpty(),
                breakdowns = tables.mapNotNull { it.await() },
            )
        }
        cache.put(cacheKey(projectId, range), snapshot)
    }

    private suspend fun fetchTable(projectId: Long, range: AnalyticsRange, breakdown: WebBreakdown): BreakdownTable? =
        when (breakdown.source) {
            BreakdownSource.WebStats ->
                api.getWebStatsTable(projectId, range.dateFrom, breakdown.apiValue, BreakdownRowLimit)
                    .getOrNull()
                    ?.toBreakdownTable(breakdown)

            BreakdownSource.EventProperty ->
                api.getPropertyBreakdown(projectId, range.dateFrom, breakdown.apiValue, BreakdownRowLimit)
                    .getOrNull()
                    ?.toPropertyBreakdownTable(cache.json, breakdown)
        }

    private fun cacheKey(projectId: Long, range: AnalyticsRange) = "analytics:$CacheVersion:$projectId:${range.name}"
}
