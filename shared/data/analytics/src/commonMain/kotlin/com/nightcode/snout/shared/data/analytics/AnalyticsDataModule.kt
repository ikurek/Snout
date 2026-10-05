package com.nightcode.snout.shared.data.analytics

import com.nightcode.snout.shared.api.analytics.AnalyticsApi
import com.nightcode.snout.shared.api.analytics.AnalyticsApiModule
import com.nightcode.snout.shared.data.common.DataCommonModule
import com.nightcode.snout.shared.data.common.JsonCacheStore
import com.nightcode.snout.shared.domain.data.analytics.AnalyticsRepository
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [AnalyticsApiModule::class, DataCommonModule::class])
class AnalyticsDataModule {
    @Single(binds = [AnalyticsRepository::class])
    fun provideAnalyticsRepository(api: AnalyticsApi, cache: JsonCacheStore): AnalyticsRepository =
        AnalyticsRepositoryImpl(api, cache)
}
