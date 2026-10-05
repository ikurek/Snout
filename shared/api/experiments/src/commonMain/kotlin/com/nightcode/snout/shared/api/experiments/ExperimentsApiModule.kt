package com.nightcode.snout.shared.api.experiments

import com.nightcode.snout.shared.api.ApiModule
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import io.ktor.client.HttpClient
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [ApiModule::class])
class ExperimentsApiModule {
    @Single
    fun experimentsApi(client: HttpClient, authDataStore: AuthDataStore) = ExperimentsApi(client, authDataStore)
}
