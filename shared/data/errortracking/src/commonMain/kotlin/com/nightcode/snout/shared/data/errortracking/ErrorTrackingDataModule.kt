package com.nightcode.snout.shared.data.errortracking

import com.nightcode.snout.shared.api.errortracking.ErrorTrackingApi
import com.nightcode.snout.shared.api.errortracking.ErrorTrackingApiModule
import com.nightcode.snout.shared.data.common.DataCommonModule
import com.nightcode.snout.shared.data.common.JsonCacheStore
import com.nightcode.snout.shared.domain.data.errortracking.ErrorTrackingRepository
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [ErrorTrackingApiModule::class, DataCommonModule::class])
class ErrorTrackingDataModule {
    @Single(binds = [ErrorTrackingRepository::class])
    fun provideErrorTrackingRepository(api: ErrorTrackingApi, cache: JsonCacheStore): ErrorTrackingRepository =
        ErrorTrackingRepositoryImpl(api, cache)
}
