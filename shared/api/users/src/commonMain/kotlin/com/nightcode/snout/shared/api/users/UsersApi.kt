package com.nightcode.snout.shared.api.users

import com.nightcode.snout.shared.api.networkCall
import com.nightcode.snout.shared.api.users.model.UserDto
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class UsersApi internal constructor(
    private val client: HttpClient,
    private val authDataStore: AuthDataStore
) {

    suspend fun getCurrentUser() = networkCall<UserDto> {
        client.get("${authDataStore.getInstanceUrl()}/api/users/@me").body()
    }
}
