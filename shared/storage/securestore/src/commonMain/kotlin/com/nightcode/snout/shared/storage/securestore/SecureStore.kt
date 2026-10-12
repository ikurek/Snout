package com.nightcode.snout.shared.storage.securestore

import com.nightcode.snout.shared.common.testdata.TestData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Strings kept encrypted under a key the platform holds: KSafe, with its key in the Keychain on iOS (this device
 * only, so never in a backup) and in the Android Keystore on Android. For secrets and personal data, not for
 * preferences.
 *
 * A value that can no longer be decrypted (a lost or invalidated key) reads as missing.
 */
interface SecureStore {
    /** Throws [SecureStoreException] when the platform can't read the store right now (e.g. before first unlock). */
    suspend fun get(key: String): String?

    /** Throws [SecureStoreException] when the platform refuses to store the value. */
    suspend fun set(key: String, value: String)

    /** Throws [SecureStoreException] when the platform refuses to delete the value. */
    suspend fun remove(key: String)
}

class SecureStoreException(message: String, cause: Throwable? = null) : IllegalStateException(message, cause)

/** A [SecureStore] kept in a map, for tests of the code built on it. */
@TestData
class InMemorySecureStore(initial: Map<String, String> = emptyMap()) : SecureStore {
    private val values = MutableStateFlow(initial)

    /** Everything stored right now. */
    val snapshot: Map<String, String> get() = values.value

    override suspend fun get(key: String): String? = values.value[key]

    override suspend fun set(key: String, value: String) = values.update { it + (key to value) }

    override suspend fun remove(key: String) = values.update { it - key }
}
