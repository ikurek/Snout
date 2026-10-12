package com.nightcode.snout.shared.storage.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nightcode.snout.shared.storage.securestore.SecureStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single

internal const val PersonalApiKeySecureKey = "personal_api_key"

private val InstanceUrlPreferenceKey = stringPreferencesKey("instance_url")

/**
 * The connection: the instance URL in DataStore, and the personal API key in the [SecureStore]. The key is loaded
 * once and then served from memory, so the HTTP client can read it per request and logging out is seen at once.
 */
@Single
class AuthDataStore(private val dataStore: DataStore<Preferences>, private val secureStore: SecureStore) {

    private val personalApiKey = MutableStateFlow<String?>(null)
    private val keyMutex = Mutex()
    private var isKeyLoaded = false

    suspend fun getPersonalApiKey(): String? = withPersonalApiKey { it }

    suspend fun savePersonalApiKey(key: String) = withPersonalApiKey {
        secureStore.set(PersonalApiKeySecureKey, key)
        personalApiKey.value = key
    }

    fun observePersonalApiKey(): Flow<String?> = flow {
        withPersonalApiKey { }
        emitAll(personalApiKey)
    }

    suspend fun getInstanceUrl(): String? = dataStore.data.map { it[InstanceUrlPreferenceKey] }
        .firstOrNull()
        ?.takeIf { it.isNotBlank() }

    suspend fun saveInstanceUrl(url: String) {
        dataStore.edit { it[InstanceUrlPreferenceKey] = url }
    }

    suspend fun clear() {
        clearPersonalApiKey()
        dataStore.edit { it.remove(InstanceUrlPreferenceKey) }
    }

    /** Drops only the key, so signing in again can start from the instance the user already chose. */
    suspend fun clearPersonalApiKey() = withPersonalApiKey {
        secureStore.remove(PersonalApiKeySecureKey)
        personalApiKey.value = null
    }

    /** Runs [block] with the current key, loading it first; serialises every change. */
    private suspend fun <T> withPersonalApiKey(block: suspend (String?) -> T): T = keyMutex.withLock {
        if (!isKeyLoaded) {
            personalApiKey.value = loadPersonalApiKey()
            isKeyLoaded = true
        }
        block(personalApiKey.value)
    }

    private suspend fun loadPersonalApiKey(): String? =
        secureStore.get(PersonalApiKeySecureKey)?.takeIf { it.isNotBlank() }
}
