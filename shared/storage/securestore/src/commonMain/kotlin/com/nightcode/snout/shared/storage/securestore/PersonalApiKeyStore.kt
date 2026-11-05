package com.nightcode.snout.shared.storage.securestore

import com.nightcode.snout.shared.common.testdata.TestData

/**
 * The personal API key, kept apart from the rest of the [SecureStore] because the iOS widget extension must read it
 * too, and KSafe serves one process only. Each platform keeps it where its widgets can reach it:
 * - Android: in KSafe, with everything else. Widgets run in the app's process and use the app's store.
 * - iOS: one Keychain item in the access group the app shares with its widget extension, which reads it on every
 *   load and never writes it.
 *
 * Every method throws [SecureStoreException] when the platform can't read, store or delete the key right now.
 */
interface PersonalApiKeyStore {
    suspend fun get(): String?

    suspend fun set(key: String)

    suspend fun clear()
}

/** The key's name in KSafe: where Android keeps it, and where iOS kept it before widgets. */
internal const val PersonalApiKeyName = "personal_api_key"

/** The key in the [SecureStore] (KSafe): on Android, and on iOS in a build that can't share the Keychain. */
internal class SecureStorePersonalApiKeyStore(private val store: SecureStore) : PersonalApiKeyStore {

    override suspend fun get(): String? = store.get(PersonalApiKeyName)

    override suspend fun set(key: String) = store.set(PersonalApiKeyName, key)

    override suspend fun clear() = store.remove(PersonalApiKeyName)
}

/** A [PersonalApiKeyStore] in memory, for tests of the code built on it. */
@TestData
class InMemoryPersonalApiKeyStore(initial: String? = null) : PersonalApiKeyStore {

    /** The key stored right now. */
    var key: String? = initial
        private set

    override suspend fun get(): String? = key

    override suspend fun set(key: String) {
        this.key = key
    }

    override suspend fun clear() {
        key = null
    }
}
