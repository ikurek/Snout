package com.nightcode.snout.shared.domain.usecase.insights

import com.nightcode.snout.shared.data.insights.InsightsDataModule
import com.nightcode.snout.shared.domain.data.insights.DashboardRepository
import com.nightcode.snout.shared.domain.data.insights.InsightRepository
import com.nightcode.snout.shared.domain.usecase.appstate.AppStateUsecaseModule
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [InsightsDataModule::class, AppStateUsecaseModule::class])
class InsightsUsecaseModule {
    @Single
    fun observeDashboards(observe: ObserveSelectedProjectId, repository: DashboardRepository) =
        ObserveDashboards(observe, repository)

    @Single
    fun refreshDashboards(get: GetSelectedProjectId, repository: DashboardRepository) = RefreshDashboards(
        get,
        repository,
    )

    @Single
    fun observeDashboard(observe: ObserveSelectedProjectId, repository: DashboardRepository) =
        ObserveDashboard(observe, repository)

    @Single
    fun refreshDashboard(get: GetSelectedProjectId, repository: DashboardRepository) = RefreshDashboard(get, repository)

    @Single
    fun refreshDashboardTile(get: GetSelectedProjectId, repository: DashboardRepository) =
        RefreshDashboardTile(get, repository)

    @Single
    fun observeInsights(observe: ObserveSelectedProjectId, repository: InsightRepository) =
        ObserveInsights(observe, repository)

    @Single
    fun refreshInsights(get: GetSelectedProjectId, repository: InsightRepository) = RefreshInsights(get, repository)

    @Single
    fun searchInsights(get: GetSelectedProjectId, repository: InsightRepository) = SearchInsights(get, repository)

    @Single
    fun getInsightDetail(get: GetSelectedProjectId, repository: InsightRepository) = GetInsightDetail(get, repository)

    @Single
    fun setInsightFavorite(get: GetSelectedProjectId, repository: InsightRepository) =
        SetInsightFavorite(get, repository)

    @Single
    fun findInsightIdByShortId(get: GetSelectedProjectId, repository: InsightRepository) =
        FindInsightIdByShortId(get, repository)
}
