package com.nightcode.snout.shared.data.auth

import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.model.auth.PersonalApiKey
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import kotlinx.coroutines.flow.Flow

internal class AuthRepositoryImpl(private val dataStore: AuthDataStore) : AuthRepository {

    override suspend fun getPersonalApiKey(): PersonalApiKey? =
        dataStore.getPersonalApiKey()?.let { PersonalApiKey(it) }

    override suspend fun savePersonalApiKey(key: PersonalApiKey) = dataStore.savePersonalApiKey(key.value)

    override suspend fun saveInstanceUrl(url: String) = dataStore.saveInstanceUrl(url)

    override suspend fun getInstanceUrl(): String? = dataStore.getInstanceUrl()

    override suspend fun clear() = dataStore.clear()

    override fun observePersonalApiKey(): Flow<String?> = dataStore.observePersonalApiKey()
}
