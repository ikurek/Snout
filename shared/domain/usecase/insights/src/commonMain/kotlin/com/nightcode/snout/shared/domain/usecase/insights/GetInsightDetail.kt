package com.nightcode.snout.shared.domain.usecase.insights

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.insights.InsightRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.insights.InsightDetail
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import org.koin.core.annotation.Single

@Single
class GetInsightDetail(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: InsightRepository,
) {
    /** Insight [insightId] of [projectId] when given (a home-screen widget names its own), else of the selected one. */
    suspend operator fun invoke(insightId: Long, projectId: Long? = null): Either<DataError, InsightDetail> =
        repository.getInsight(projectId ?: getSelectedProjectId(), insightId)
}
