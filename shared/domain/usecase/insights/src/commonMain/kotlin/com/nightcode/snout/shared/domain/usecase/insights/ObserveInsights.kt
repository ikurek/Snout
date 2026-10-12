package com.nightcode.snout.shared.domain.usecase.insights

import com.nightcode.snout.shared.domain.data.insights.InsightRepository
import com.nightcode.snout.shared.domain.model.insights.InsightSummary
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class ObserveInsights(
    private val observeSelectedProjectId: ObserveSelectedProjectId,
    private val repository: InsightRepository,
) {
    operator fun invoke(): Flow<List<InsightSummary>?> =
        observeSelectedProjectId.flatMapProject { repository.observeInsights(it) }
}
