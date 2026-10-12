package com.nightcode.snout.shared.domain.usecase.insights

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.insights.InsightRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.insights.InsightSummary
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import org.koin.core.annotation.Single

@Single
class SearchInsights(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: InsightRepository,
) {
    suspend operator fun invoke(query: String): Either<DataError, List<InsightSummary>> =
        repository.searchInsights(getSelectedProjectId(), query)
}
