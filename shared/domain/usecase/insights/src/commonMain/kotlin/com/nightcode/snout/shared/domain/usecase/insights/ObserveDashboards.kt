package com.nightcode.snout.shared.domain.usecase.insights

import com.nightcode.snout.shared.domain.data.insights.DashboardRepository
import com.nightcode.snout.shared.domain.model.insights.DashboardSummary
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class ObserveDashboards(
    private val observeSelectedProjectId: ObserveSelectedProjectId,
    private val repository: DashboardRepository,
) {
    operator fun invoke(): Flow<List<DashboardSummary>?> =
        observeSelectedProjectId.flatMapProject { repository.observeDashboards(it) }
}
