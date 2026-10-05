package com.nightcode.snout.shared.api.insights

import com.nightcode.snout.shared.api.ApiModule
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import io.ktor.client.HttpClient
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [ApiModule::class])
class InsightsApiModule {
    @Single
    fun dashboardsApi(client: HttpClient, authDataStore: AuthDataStore) = DashboardsApi(client, authDataStore)

    @Single
    fun insightsApi(client: HttpClient, authDataStore: AuthDataStore) = InsightsApi(client, authDataStore)
}
