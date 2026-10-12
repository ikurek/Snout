package com.nightcode.snout.shared.api.analytics

import com.nightcode.snout.shared.api.ApiModule
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import io.ktor.client.HttpClient
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [ApiModule::class])
class AnalyticsApiModule {
    @Single
    fun provideAnalyticsApi(client: HttpClient, authDataStore: AuthDataStore) = AnalyticsApi(client, authDataStore)
}
