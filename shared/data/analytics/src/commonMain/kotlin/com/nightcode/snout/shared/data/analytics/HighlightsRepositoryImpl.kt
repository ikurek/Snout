package com.nightcode.snout.shared.data.analytics

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.either
import com.nightcode.snout.shared.api.analytics.AnalyticsApi
import com.nightcode.snout.shared.data.analytics.mapper.toActiveUserHighlights
import com.nightcode.snout.shared.data.analytics.mapper.toExceptionsHighlight
import com.nightcode.snout.shared.data.analytics.mapper.toLatestAppVersion
import com.nightcode.snout.shared.data.analytics.mapper.toRevenueHighlights
import com.nightcode.snout.shared.domain.data.analytics.HighlightsRepository
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.Highlight
import com.nightcode.snout.shared.domain.model.analytics.HighlightMetric
import com.nightcode.snout.shared.domain.model.common.DataError
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single(binds = [HighlightsRepository::class])
internal class HighlightsRepositoryImpl(private val api: AnalyticsApi, private val json: Json) : HighlightsRepository {

    override suspend fun getActiveUsers(projectId: Long): Either<DataError, Map<HighlightMetric, Highlight>> =
        api.getActiveUserCounts(projectId).mapping { it.toActiveUserHighlights(json) }

    override suspend fun getRevenue(projectId: Long): Either<DataError, Map<HighlightMetric, Highlight>> = either {
        val currency = api.getProjectSettings(projectId).bind().baseCurrency
        api.getRevenueTotals(projectId).mapping { it.toRevenueHighlights(json, currency) }.bind()
    }

    override suspend fun getLatestAppVersion(projectId: Long): Either<DataError, Highlight> =
        api.getAppVersions(projectId).mapping { it.toLatestAppVersion(json) }

    override suspend fun getExceptions(projectId: Long, range: AnalyticsRange): Either<DataError, Highlight> =
        api.getExceptionCounts(projectId, range.dateFrom).mapping { it.toExceptionsHighlight(json) }

    /** Maps a response, failing with [DataError.UnexpectedError] when PostHog answered with a shape it can't read. */
    private inline fun <T, R> Either<DataError, T>.mapping(crossinline map: (T) -> R): Either<DataError, R> =
        flatMap { response -> Either.catch { map(response) }.mapLeft { DataError.UnexpectedError } }
}
