package com.nightcode.snout.shared.storage.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nightcode.snout.shared.storage.securestore.PersonalApiKeyStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single

private val InstanceUrlPreferenceKey = stringPreferencesKey("instance_url")

/**
 * The connection: the instance URL in DataStore, and the personal API key in its [PersonalApiKeyStore]. The key is
 * loaded once and then served from memory, so the HTTP client can read it per request and logging out is seen at
 * once.
 */
@Single
class AuthDataStore(private val dataStore: DataStore<Preferences>, private val apiKeyStore: PersonalApiKeyStore) {

    private val personalApiKey = MutableStateFlow<String?>(null)
    private val keyMutex = Mutex()
    private var isKeyLoaded = false

    suspend fun getPersonalApiKey(): String? = withPersonalApiKey { it }

    suspend fun savePersonalApiKey(key: String) = withPersonalApiKey {
        apiKeyStore.set(key)
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
        apiKeyStore.clear()
        personalApiKey.value = null
    }

    /**
     * Reads the key from its store again, for a process that shares the store with one that changes it: the
     * iOS widget extension, which may have loaded the key before the app signed out or in.
     */
    suspend fun reloadPersonalApiKey() = keyMutex.withLock {
        personalApiKey.value = loadPersonalApiKey()
        isKeyLoaded = true
    }

    /** Runs [block] with the current key, loading it first; serialises every change. */
    private suspend fun <T> withPersonalApiKey(block: suspend (String?) -> T): T = keyMutex.withLock {
        if (!isKeyLoaded) {
            personalApiKey.value = loadPersonalApiKey()
            isKeyLoaded = true
        }
        block(personalApiKey.value)
    }

    /**
     * The stored key, unless the instance it belongs to is gone. iOS keeps Keychain items after the app is deleted
     * while the preferences go with it, so a reinstalled app finds a key without its instance: that one is dropped,
     * not used to sign the new install in.
     */
    private suspend fun loadPersonalApiKey(): String? {
        val key = apiKeyStore.get()?.takeIf { it.isNotBlank() } ?: return null
        val hasInstance = getInstanceUrl() != null
        if (!hasInstance) apiKeyStore.clear()
        return key.takeIf { hasInstance }
    }
}
