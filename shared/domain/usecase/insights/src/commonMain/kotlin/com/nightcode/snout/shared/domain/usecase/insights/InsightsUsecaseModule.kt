package com.nightcode.snout.shared.domain.usecase.insights

import com.nightcode.snout.shared.domain.data.insights.DashboardRepository
import com.nightcode.snout.shared.domain.data.insights.InsightRepository
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class InsightsUsecaseModule {
    @Single
    fun provideObserveDashboards(observe: ObserveSelectedProjectId, repository: DashboardRepository) =
        ObserveDashboards(observe, repository)

    @Single
    fun provideRefreshDashboards(get: GetSelectedProjectId, repository: DashboardRepository) = RefreshDashboards(
        get,
        repository,
    )

    @Single
    fun provideObserveDashboard(observe: ObserveSelectedProjectId, repository: DashboardRepository) =
        ObserveDashboard(observe, repository)

    @Single
    fun provideRefreshDashboard(get: GetSelectedProjectId, repository: DashboardRepository) =
        RefreshDashboard(get, repository)

    @Single
    fun provideRefreshDashboardTile(get: GetSelectedProjectId, repository: DashboardRepository) =
        RefreshDashboardTile(get, repository)

    @Single
    fun provideObserveInsights(observe: ObserveSelectedProjectId, repository: InsightRepository) =
        ObserveInsights(observe, repository)

    @Single
    fun provideRefreshInsights(get: GetSelectedProjectId, repository: InsightRepository) =
        RefreshInsights(get, repository)

    @Single
    fun provideSearchInsights(get: GetSelectedProjectId, repository: InsightRepository) =
        SearchInsights(get, repository)

    @Single
    fun provideGetInsightDetail(get: GetSelectedProjectId, repository: InsightRepository) =
        GetInsightDetail(get, repository)

    @Single
    fun provideSetInsightFavorite(get: GetSelectedProjectId, repository: InsightRepository) =
        SetInsightFavorite(get, repository)

    @Single
    fun provideFindInsightIdByShortId(get: GetSelectedProjectId, repository: InsightRepository) =
        FindInsightIdByShortId(get, repository)
}
