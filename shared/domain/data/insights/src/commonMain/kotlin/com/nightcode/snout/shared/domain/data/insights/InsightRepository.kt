package com.nightcode.snout.shared.domain.data.insights

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.insights.InsightDetail
import com.nightcode.snout.shared.domain.model.insights.InsightSummary
import kotlinx.coroutines.flow.Flow

interface InsightRepository {
    /** Emits after each successful change made through this app, so lists showing the old state load again. */
    val changes: Flow<Unit>

    suspend fun getInsights(projectId: Long): Either<DataError, List<InsightSummary>>
    suspend fun searchInsights(projectId: Long, query: String): Either<DataError, List<InsightSummary>>
    suspend fun getInsight(projectId: Long, insightId: Long): Either<DataError, InsightDetail>

    /** The id of the insight with [shortId], or null when the project has none. */
    suspend fun findInsightId(projectId: Long, shortId: String): Either<DataError, Long?>
    suspend fun setFavorite(projectId: Long, insightId: Long, favorited: Boolean): Either<DataError, InsightSummary>
}
