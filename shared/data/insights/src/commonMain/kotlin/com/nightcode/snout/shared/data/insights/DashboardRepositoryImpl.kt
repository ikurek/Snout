package com.nightcode.snout.shared.data.insights

import arrow.core.Either
import com.nightcode.snout.shared.api.insights.DashboardsApi
import com.nightcode.snout.shared.api.insights.InsightsApi
import com.nightcode.snout.shared.data.common.toDataError
import com.nightcode.snout.shared.data.insights.mapper.toDashboard
import com.nightcode.snout.shared.data.insights.mapper.toResult
import com.nightcode.snout.shared.data.insights.mapper.toSummary
import com.nightcode.snout.shared.domain.data.insights.DashboardRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.insights.Dashboard
import com.nightcode.snout.shared.domain.model.insights.DashboardSummary
import com.nightcode.snout.shared.domain.model.insights.InsightResult
import org.koin.core.annotation.Single

@Single(binds = [DashboardRepository::class])
internal class DashboardRepositoryImpl(private val dashboardsApi: DashboardsApi, private val insightsApi: InsightsApi) :
    DashboardRepository {

    override suspend fun getDashboards(projectId: Long): Either<DataError, List<DashboardSummary>> = dashboardsApi
        .getDashboards(projectId)
        .mapLeft { it.toDataError() }
        .map { response -> response.results.map { it.toSummary() } }

    override suspend fun getDashboard(projectId: Long, dashboardId: Long): Either<DataError, Dashboard> =
        dashboardsApi.getDashboard(projectId, dashboardId).mapLeft { it.toDataError() }.map { it.toDashboard() }

    override suspend fun getTileResult(
        projectId: Long,
        dashboardId: Long,
        insightId: Long,
    ): Either<DataError, InsightResult?> = insightsApi.getInsight(projectId, insightId, fromDashboardId = dashboardId)
        .mapLeft { it.toDataError() }
        .map { it.toResult() }
}
