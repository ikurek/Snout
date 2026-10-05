package com.nightcode.snout.shared.data.insights

import com.nightcode.snout.shared.api.insights.DashboardsApi
import com.nightcode.snout.shared.api.insights.InsightsApi
import com.nightcode.snout.shared.api.insights.InsightsApiModule
import com.nightcode.snout.shared.data.common.DataCommonModule
import com.nightcode.snout.shared.data.common.JsonCacheStore
import com.nightcode.snout.shared.domain.data.insights.DashboardRepository
import com.nightcode.snout.shared.domain.data.insights.InsightRepository
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [InsightsApiModule::class, DataCommonModule::class])
class InsightsDataModule {
    @Single(binds = [DashboardRepository::class])
    fun provideDashboardRepository(
        dashboardsApi: DashboardsApi,
        insightsApi: InsightsApi,
        cache: JsonCacheStore,
    ): DashboardRepository = DashboardRepositoryImpl(dashboardsApi, insightsApi, cache)

    @Single(binds = [InsightRepository::class])
    fun provideInsightRepository(api: InsightsApi, cache: JsonCacheStore): InsightRepository =
        InsightRepositoryImpl(api, cache)
}
