package com.nightcode.snout.shared.domain.usecase.insights

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.insights.InsightRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.insights.InsightDetail
import com.nightcode.snout.shared.domain.model.insights.InsightSummary
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveInsights(
    private val observeSelectedProjectId: ObserveSelectedProjectId,
    private val repository: InsightRepository,
) {
    operator fun invoke(): Flow<List<InsightSummary>?> =
        observeSelectedProjectId().flatMapLatest { repository.observeInsights(it) }
}

class RefreshInsights(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: InsightRepository,
) {
    suspend operator fun invoke(): Either<DataError, Unit> = repository.refreshInsights(getSelectedProjectId())
}

class SearchInsights(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: InsightRepository,
) {
    suspend operator fun invoke(query: String): Either<DataError, List<InsightSummary>> =
        repository.searchInsights(getSelectedProjectId(), query)
}

class GetInsightDetail(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: InsightRepository,
) {
    suspend operator fun invoke(insightId: Long): Either<DataError, InsightDetail> =
        repository.getInsight(getSelectedProjectId(), insightId)
}

class SetInsightFavorite(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: InsightRepository,
) {
    suspend operator fun invoke(insightId: Long, favorited: Boolean): Either<DataError, InsightSummary> =
        repository.setFavorite(getSelectedProjectId(), insightId, favorited)
}
