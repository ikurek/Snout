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
    suspend operator fun invoke(insightId: Long): Either<DataError, InsightDetail> =
        repository.getInsight(getSelectedProjectId(), insightId)
}
