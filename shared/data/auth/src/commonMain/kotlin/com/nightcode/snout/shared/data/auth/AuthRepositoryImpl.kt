package com.nightcode.snout.shared.data.auth

import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.model.auth.PersonalApiKey
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single(binds = [AuthRepository::class])
internal class AuthRepositoryImpl(private val dataStore: AuthDataStore) : AuthRepository {

    override suspend fun getPersonalApiKey(): PersonalApiKey? = dataStore.getPersonalApiKey()?.let(::PersonalApiKey)

    override suspend fun savePersonalApiKey(key: PersonalApiKey) = dataStore.savePersonalApiKey(key.value)

    override suspend fun saveInstanceUrl(url: String) = dataStore.saveInstanceUrl(url)

    override suspend fun getInstanceUrl(): String? = dataStore.getInstanceUrl()

    override suspend fun clear() = dataStore.clear()

    override fun observePersonalApiKey(): Flow<PersonalApiKey?> =
        dataStore.observePersonalApiKey().map { key -> key?.let(::PersonalApiKey) }
}
