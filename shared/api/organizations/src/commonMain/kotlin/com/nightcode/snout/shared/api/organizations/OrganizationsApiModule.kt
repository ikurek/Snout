package com.nightcode.snout.shared.api.organizations

import com.nightcode.snout.shared.api.ApiModule
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import io.ktor.client.HttpClient
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [ApiModule::class])
class OrganizationsApiModule {
    @Single
    fun organizationsApi(client: HttpClient, authDataStore: AuthDataStore) = OrganizationsApi(client, authDataStore)
}
