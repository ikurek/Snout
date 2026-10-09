package com.nightcode.snout.shared.api.errortracking

import com.nightcode.snout.shared.api.ApiModule
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import io.ktor.client.HttpClient
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [ApiModule::class])
class ErrorTrackingApiModule {
    @Single
    fun errorTrackingApi(client: HttpClient, authDataStore: AuthDataStore) = ErrorTrackingApi(client, authDataStore)
}
