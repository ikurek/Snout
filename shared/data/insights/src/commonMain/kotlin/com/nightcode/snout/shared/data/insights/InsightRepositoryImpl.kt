package com.nightcode.snout.shared.data.insights

import arrow.core.Either
import com.nightcode.snout.shared.api.insights.InsightsApi
import com.nightcode.snout.shared.data.common.ChangeSignal
import com.nightcode.snout.shared.data.common.toDataError
import com.nightcode.snout.shared.data.insights.mapper.toDetail
import com.nightcode.snout.shared.data.insights.mapper.toSummary
import com.nightcode.snout.shared.domain.data.insights.InsightRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.insights.InsightDetail
import com.nightcode.snout.shared.domain.model.insights.InsightSummary
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single(binds = [InsightRepository::class])
internal class InsightRepositoryImpl(private val api: InsightsApi) : InsightRepository {

    private val changeSignal = ChangeSignal()
    override val changes: Flow<Unit> = changeSignal.changes

    override suspend fun getInsights(projectId: Long): Either<DataError, List<InsightSummary>> =
        api.getInsights(projectId)
            .mapLeft { it.toDataError() }
            .map { response -> response.results.map { it.toSummary() } }

    override suspend fun searchInsights(projectId: Long, query: String): Either<DataError, List<InsightSummary>> =
        api.getInsights(projectId, search = query)
            .mapLeft { it.toDataError() }
            .map { response -> response.results.map { it.toSummary() } }

    override suspend fun getInsight(projectId: Long, insightId: Long): Either<DataError, InsightDetail> =
        api.getInsight(projectId, insightId).mapLeft { it.toDataError() }.map { it.toDetail() }

    override suspend fun findInsightId(projectId: Long, shortId: String): Either<DataError, Long?> =
        api.findInsightsByShortId(projectId, shortId)
            .mapLeft { it.toDataError() }
            .map { response -> response.results.firstOrNull { it.shortId == shortId }?.id }

    override suspend fun setFavorite(
        projectId: Long,
        insightId: Long,
        favorited: Boolean,
    ): Either<DataError, InsightSummary> = api.setFavorite(projectId, insightId, favorited)
        .mapLeft { it.toDataError() }
        .map { it.toSummary() }
        .onRight { changeSignal.emit() }
}
