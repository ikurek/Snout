package com.nightcode.snout.shared.api.logs

import com.nightcode.snout.shared.api.ApiModule
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import io.ktor.client.HttpClient
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [ApiModule::class])
class LogsApiModule {
    @Single
    fun provideLogsApi(client: HttpClient, authDataStore: AuthDataStore) = LogsApi(client, authDataStore)
}
