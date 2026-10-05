package com.nightcode.snout.shared.data.insights

import arrow.core.Either
import com.nightcode.snout.shared.api.insights.InsightsApi
import com.nightcode.snout.shared.data.common.JsonCacheStore
import com.nightcode.snout.shared.data.common.toDataError
import com.nightcode.snout.shared.data.insights.mapper.toDetail
import com.nightcode.snout.shared.data.insights.mapper.toSummary
import com.nightcode.snout.shared.domain.data.insights.InsightRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.insights.InsightDetail
import com.nightcode.snout.shared.domain.model.insights.InsightSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

internal class InsightRepositoryImpl(private val api: InsightsApi, private val cache: JsonCacheStore) :
    InsightRepository {

    override fun observeInsights(projectId: Long): Flow<List<InsightSummary>?> = cache.observe(listKey(projectId))

    override suspend fun refreshInsights(projectId: Long): Either<DataError, Unit> = api.getInsights(projectId)
        .mapLeft { it.toDataError() }
        .map { response -> cache.put(listKey(projectId), response.results.map { it.toSummary() }) }

    override suspend fun searchInsights(projectId: Long, query: String): Either<DataError, List<InsightSummary>> =
        api.getInsights(projectId, search = query)
            .mapLeft { it.toDataError() }
            .map { response -> response.results.map { it.toSummary() } }

    override suspend fun getInsight(projectId: Long, insightId: Long): Either<DataError, InsightDetail> =
        api.getInsight(projectId, insightId).mapLeft { it.toDataError() }.map { it.toDetail() }

    override suspend fun setFavorite(
        projectId: Long,
        insightId: Long,
        favorited: Boolean,
    ): Either<DataError, InsightSummary> = api.setFavorite(projectId, insightId, favorited)
        .mapLeft { it.toDataError() }
        .map { it.toSummary() }
        .onRight { updated ->
            val cached = cache.observe<List<InsightSummary>>(listKey(projectId)).first() ?: return@onRight
            cache.put(listKey(projectId), cached.map { if (it.id == updated.id) updated else it })
        }

    private fun listKey(projectId: Long) = "insights:$projectId"
}
