package com.nightcode.snout.shared.data.logs

import com.nightcode.snout.shared.api.logs.LogsApi
import com.nightcode.snout.shared.api.logs.LogsApiModule
import com.nightcode.snout.shared.data.common.DataCommonModule
import com.nightcode.snout.shared.data.common.JsonCacheStore
import com.nightcode.snout.shared.domain.data.logs.LogsRepository
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [LogsApiModule::class, DataCommonModule::class])
class LogsDataModule {
    @Single(binds = [LogsRepository::class])
    fun provideLogsRepository(api: LogsApi, cache: JsonCacheStore): LogsRepository = LogsRepositoryImpl(api, cache)
}
