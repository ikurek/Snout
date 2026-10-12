package com.nightcode.snout.shared.domain.usecase.insights

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.insights.InsightRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import org.koin.core.annotation.Single

/** Insight links use the short id from the web app; screens need the numeric id. */
@Single
class FindInsightIdByShortId(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: InsightRepository,
) {
    suspend operator fun invoke(shortId: String): Either<DataError, Long?> =
        repository.findInsightId(getSelectedProjectId(), shortId)
}
