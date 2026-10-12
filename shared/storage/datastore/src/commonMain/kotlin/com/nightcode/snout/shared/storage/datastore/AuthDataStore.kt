package com.nightcode.snout.shared.storage.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val PersonalApiKeyPreferenceKey = stringPreferencesKey("personal_api_key")
private val InstanceUrlPreferenceKey = stringPreferencesKey("instance_url")

class AuthDataStore internal constructor(private val dataStore: DataStore<Preferences>) {
    suspend fun getPersonalApiKey(): String? = dataStore.data.map { it[PersonalApiKeyPreferenceKey] }
        .firstOrNull()
        ?.takeIf { it.isBlank().not() }

    suspend fun savePersonalApiKey(key: String) {
        dataStore.edit { it[PersonalApiKeyPreferenceKey] = key }
    }

    suspend fun getInstanceUrl(): String? = dataStore.data.map { it[InstanceUrlPreferenceKey] }
        .firstOrNull()
        ?.takeIf { it.isBlank().not() }

    suspend fun saveInstanceUrl(key: String) {
        dataStore.edit { it[InstanceUrlPreferenceKey] = key }
    }

    suspend fun clear() {
        dataStore.edit {
            it.remove(PersonalApiKeyPreferenceKey)
            it.remove(InstanceUrlPreferenceKey)
        }
    }

    /** Drops only the key, so signing in again can start from the instance the user already chose. */
    suspend fun clearPersonalApiKey() {
        dataStore.edit { it.remove(PersonalApiKeyPreferenceKey) }
    }

    fun observePersonalApiKey(): Flow<String?> =
        dataStore.data.map { it[PersonalApiKeyPreferenceKey]?.takeIf { key -> key.isNotBlank() } }
}
