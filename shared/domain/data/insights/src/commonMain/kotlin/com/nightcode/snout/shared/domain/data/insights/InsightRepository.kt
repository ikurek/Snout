package com.nightcode.snout.shared.domain.data.insights

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.insights.InsightDetail
import com.nightcode.snout.shared.domain.model.insights.InsightSummary
import kotlinx.coroutines.flow.Flow

interface InsightRepository {
    fun observeInsights(projectId: Long): Flow<List<InsightSummary>?>
    suspend fun refreshInsights(projectId: Long): Either<DataError, Unit>
    suspend fun searchInsights(projectId: Long, query: String): Either<DataError, List<InsightSummary>>
    suspend fun getInsight(projectId: Long, insightId: Long): Either<DataError, InsightDetail>
    suspend fun setFavorite(projectId: Long, insightId: Long, favorited: Boolean): Either<DataError, InsightSummary>
}
