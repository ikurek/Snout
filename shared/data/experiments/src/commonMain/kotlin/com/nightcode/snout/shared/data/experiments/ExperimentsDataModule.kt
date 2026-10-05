package com.nightcode.snout.shared.data.experiments

import com.nightcode.snout.shared.api.experiments.ExperimentsApi
import com.nightcode.snout.shared.api.experiments.ExperimentsApiModule
import com.nightcode.snout.shared.data.common.DataCommonModule
import com.nightcode.snout.shared.data.common.JsonCacheStore
import com.nightcode.snout.shared.domain.data.experiments.ExperimentRepository
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [ExperimentsApiModule::class, DataCommonModule::class])
class ExperimentsDataModule {
    @Single(binds = [ExperimentRepository::class])
    fun provideExperimentRepository(api: ExperimentsApi, cache: JsonCacheStore): ExperimentRepository =
        ExperimentRepositoryImpl(api, cache)
}
