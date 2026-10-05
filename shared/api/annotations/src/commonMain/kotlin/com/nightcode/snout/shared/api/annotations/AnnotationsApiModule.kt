package com.nightcode.snout.shared.api.annotations

import com.nightcode.snout.shared.api.ApiModule
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import io.ktor.client.HttpClient
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [ApiModule::class])
class AnnotationsApiModule {
    @Single
    fun annotationsApi(client: HttpClient, authDataStore: AuthDataStore) = AnnotationsApi(client, authDataStore)
}
