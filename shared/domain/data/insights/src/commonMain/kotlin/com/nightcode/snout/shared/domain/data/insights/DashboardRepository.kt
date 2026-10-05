package com.nightcode.snout.shared.domain.data.insights

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.insights.Dashboard
import com.nightcode.snout.shared.domain.model.insights.DashboardSummary
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun observeDashboards(projectId: Long): Flow<List<DashboardSummary>?>
    suspend fun refreshDashboards(projectId: Long): Either<DataError, Unit>
    fun observeDashboard(projectId: Long, dashboardId: Long): Flow<Dashboard?>
    suspend fun refreshDashboard(projectId: Long, dashboardId: Long): Either<DataError, Unit>

    /** Computes one tile's insight and stores the result into the cached dashboard. */
    suspend fun refreshTile(projectId: Long, dashboardId: Long, insightId: Long): Either<DataError, Unit>
}
