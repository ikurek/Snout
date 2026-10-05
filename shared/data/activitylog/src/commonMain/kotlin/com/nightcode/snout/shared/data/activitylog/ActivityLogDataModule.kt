package com.nightcode.snout.shared.data.activitylog

import com.nightcode.snout.shared.api.activitylog.ActivityLogApi
import com.nightcode.snout.shared.api.activitylog.ActivityLogApiModule
import com.nightcode.snout.shared.data.common.DataCommonModule
import com.nightcode.snout.shared.data.common.JsonCacheStore
import com.nightcode.snout.shared.domain.data.activitylog.ActivityLogRepository
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [ActivityLogApiModule::class, DataCommonModule::class])
class ActivityLogDataModule {
    @Single(binds = [ActivityLogRepository::class])
    fun provideActivityLogRepository(api: ActivityLogApi, cache: JsonCacheStore): ActivityLogRepository =
        ActivityLogRepositoryImpl(api, cache)
}
