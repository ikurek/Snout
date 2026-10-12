package com.nightcode.snout.shared.domain.usecase.insights

import com.nightcode.snout.shared.domain.data.insights.DashboardRepository
import com.nightcode.snout.shared.domain.model.insights.Dashboard
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class ObserveDashboard(
    private val observeSelectedProjectId: ObserveSelectedProjectId,
    private val repository: DashboardRepository,
) {
    operator fun invoke(dashboardId: Long): Flow<Dashboard?> =
        observeSelectedProjectId.flatMapProject { repository.observeDashboard(it, dashboardId) }
}
