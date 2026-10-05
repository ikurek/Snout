package com.nightcode.snout.shared.data.featureflags

import com.nightcode.snout.shared.api.featureflags.FeatureFlagsApi
import com.nightcode.snout.shared.api.featureflags.FeatureFlagsApiModule
import com.nightcode.snout.shared.data.common.DataCommonModule
import com.nightcode.snout.shared.data.common.JsonCacheStore
import com.nightcode.snout.shared.domain.data.featureflags.FeatureFlagRepository
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [FeatureFlagsApiModule::class, DataCommonModule::class])
class FeatureFlagsDataModule {
    @Single(binds = [FeatureFlagRepository::class])
    fun provideFeatureFlagRepository(api: FeatureFlagsApi, cache: JsonCacheStore): FeatureFlagRepository =
        FeatureFlagRepositoryImpl(api, cache)
}
