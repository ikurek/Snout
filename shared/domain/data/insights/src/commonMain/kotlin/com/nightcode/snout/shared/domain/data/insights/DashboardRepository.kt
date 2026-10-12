package com.nightcode.snout.shared.domain.data.insights

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.insights.Dashboard
import com.nightcode.snout.shared.domain.model.insights.DashboardSummary
import com.nightcode.snout.shared.domain.model.insights.InsightResult

interface DashboardRepository {
    suspend fun getDashboards(projectId: Long): Either<DataError, List<DashboardSummary>>

    /** The dashboard with the tile results PostHog already has; tiles without one have a null result. */
    suspend fun getDashboard(projectId: Long, dashboardId: Long): Either<DataError, Dashboard>

    /** Computes one tile's insight in the context of its dashboard; null when it has no result to show. */
    suspend fun getTileResult(projectId: Long, dashboardId: Long, insightId: Long): Either<DataError, InsightResult?>
}
