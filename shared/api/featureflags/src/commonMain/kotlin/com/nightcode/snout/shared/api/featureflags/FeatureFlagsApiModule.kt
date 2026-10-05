package com.nightcode.snout.shared.api.featureflags

import com.nightcode.snout.shared.api.ApiModule
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import io.ktor.client.HttpClient
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [ApiModule::class])
class FeatureFlagsApiModule {
    @Single
    fun featureFlagsApi(client: HttpClient, authDataStore: AuthDataStore) = FeatureFlagsApi(client, authDataStore)
}
