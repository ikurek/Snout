package com.nightcode.snout.shared.api.activitylog

import com.nightcode.snout.shared.api.ApiModule
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import io.ktor.client.HttpClient
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [ApiModule::class])
class ActivityLogApiModule {
    @Single
    fun provideActivityLogApi(client: HttpClient, authDataStore: AuthDataStore) = ActivityLogApi(client, authDataStore)
}
