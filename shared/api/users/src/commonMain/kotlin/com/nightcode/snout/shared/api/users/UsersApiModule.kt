package com.nightcode.snout.shared.api.users

import com.nightcode.snout.shared.api.ApiModule
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import io.ktor.client.HttpClient
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [ApiModule::class])
class UsersApiModule {
    @Single
    fun provideUsersApi(client: HttpClient, authDataStore: AuthDataStore) = UsersApi(client, authDataStore)
}
