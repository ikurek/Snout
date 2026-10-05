package com.nightcode.snout.shared.domain.usecase.insights

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.insights.DashboardRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.insights.Dashboard
import com.nightcode.snout.shared.domain.model.insights.DashboardSummary
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveDashboards(
    private val observeSelectedProjectId: ObserveSelectedProjectId,
    private val repository: DashboardRepository,
) {
    operator fun invoke(): Flow<List<DashboardSummary>?> =
        observeSelectedProjectId().flatMapLatest { repository.observeDashboards(it) }
}

class RefreshDashboards(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: DashboardRepository,
) {
    suspend operator fun invoke(): Either<DataError, Unit> = repository.refreshDashboards(getSelectedProjectId())
}

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveDashboard(
    private val observeSelectedProjectId: ObserveSelectedProjectId,
    private val repository: DashboardRepository,
) {
    operator fun invoke(dashboardId: Long): Flow<Dashboard?> =
        observeSelectedProjectId().flatMapLatest { repository.observeDashboard(it, dashboardId) }
}

class RefreshDashboard(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: DashboardRepository,
) {
    suspend operator fun invoke(dashboardId: Long): Either<DataError, Unit> =
        repository.refreshDashboard(getSelectedProjectId(), dashboardId)
}

class RefreshDashboardTile(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: DashboardRepository,
) {
    suspend operator fun invoke(dashboardId: Long, insightId: Long): Either<DataError, Unit> =
        repository.refreshTile(getSelectedProjectId(), dashboardId, insightId)
}
