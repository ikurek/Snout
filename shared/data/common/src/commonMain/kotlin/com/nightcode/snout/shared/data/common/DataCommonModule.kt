package com.nightcode.snout.shared.data.common

import com.nightcode.snout.shared.api.ApiModule
import com.nightcode.snout.shared.domain.data.appstate.CacheRepository
import com.nightcode.snout.shared.storage.database.DatabaseModule
import com.nightcode.snout.shared.storage.database.JsonCache
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [ApiModule::class, DatabaseModule::class])
class DataCommonModule {
    @Single(binds = [JsonCacheStore::class, CacheRepository::class])
    fun jsonCacheStore(json: Json, cache: JsonCache) = JsonCacheStore(json, cache)
}
