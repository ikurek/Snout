package com.nightcode.snout.shared.data.analytics

import arrow.core.Either
import arrow.core.flatMap
import com.nightcode.snout.shared.api.analytics.AnalyticsApi
import com.nightcode.snout.shared.data.analytics.mapper.toActiveUserMetrics
import com.nightcode.snout.shared.data.analytics.mapper.toBreakdownTable
import com.nightcode.snout.shared.data.analytics.mapper.toMetricTrends
import com.nightcode.snout.shared.data.analytics.mapper.toOverviewMetrics
import com.nightcode.snout.shared.data.analytics.mapper.toPropertyBreakdownTable
import com.nightcode.snout.shared.domain.data.analytics.AnalyticsRepository
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.BreakdownSource
import com.nightcode.snout.shared.domain.model.analytics.BreakdownTable
import com.nightcode.snout.shared.domain.model.analytics.MetricTrend
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetric
import com.nightcode.snout.shared.domain.model.analytics.WebBreakdown
import com.nightcode.snout.shared.domain.model.common.DataError
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

private const val BreakdownRowLimit = 8

@Single(binds = [AnalyticsRepository::class])
internal class AnalyticsRepositoryImpl(private val api: AnalyticsApi, private val json: Json) :
    AnalyticsRepository {

    override suspend fun getOverview(projectId: Long, range: AnalyticsRange): Either<DataError, List<OverviewMetric>> =
        api.getWebOverview(projectId, range.dateFrom).mapping { it.toOverviewMetrics(json) }

    override suspend fun getActiveUsers(
        projectId: Long,
        range: AnalyticsRange,
    ): Either<DataError, List<OverviewMetric>> =
        api.getActiveUsers(projectId, range.dateFrom).mapping { it.toActiveUserMetrics(json) }

    override suspend fun getTrends(projectId: Long, range: AnalyticsRange): Either<DataError, List<MetricTrend>> =
        api.getMetricTrends(projectId, range.dateFrom, range.interval).mapping { it.toMetricTrends(json) }

    override suspend fun getBreakdown(
        projectId: Long,
        range: AnalyticsRange,
        breakdown: WebBreakdown,
    ): Either<DataError, BreakdownTable> = when (breakdown.source) {
        BreakdownSource.WebStats ->
            api.getWebStatsTable(projectId, range.dateFrom, breakdown.apiValue, BreakdownRowLimit)
                .mapping { it.toBreakdownTable(breakdown) }

        BreakdownSource.EventProperty ->
            api.getPropertyBreakdown(projectId, range.dateFrom, breakdown.apiValue, BreakdownRowLimit)
                .mapping { it.toPropertyBreakdownTable(json, breakdown) }
    }

    /** Maps a response, failing with [DataError.UnexpectedError] when PostHog answered with a shape it can't read. */
    private inline fun <T, R> Either<DataError, T>.mapping(crossinline map: (T) -> R): Either<DataError, R> =
        flatMap { response -> Either.catch { map(response) }.mapLeft { DataError.UnexpectedError } }
}
