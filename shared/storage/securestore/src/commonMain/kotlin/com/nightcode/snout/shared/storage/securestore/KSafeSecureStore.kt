package com.nightcode.snout.shared.storage.securestore

import eu.anifantakis.lib.ksafe.KSafe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

/** File name of the KSafe store; its own file, so the store's keys and `clearAll` never touch anything else. */
internal const val KSafeFileName = "secure"

/**
 * A [SecureStore] on [KSafe]: every value written encrypted (AES-GCM) into KSafe's own file, under a key held by the
 * Keychain on iOS and the Android Keystore on Android. The app depends on [SecureStore] only, so the library stays
 * an implementation detail of this module and the code built on it is tested with [InMemorySecureStore].
 */
internal class KSafeSecureStore(private val ksafe: KSafe) : SecureStore {

    override suspend fun get(key: String): String? = call("read", key) { ksafe.get<String?>(key, null) }

    override suspend fun set(key: String, value: String) = call("store", key) { ksafe.put(key, value) }

    override suspend fun remove(key: String) = call("delete", key) { ksafe.delete(key) }

    /** Runs [block] off the main thread (it can wait on the Keychain or Keystore), reporting failures uniformly. */
    @Suppress("TooGenericExceptionCaught") // KSafe documents no exception types; any failure means the same here.
    private suspend fun <T> call(action: String, key: String, block: suspend () -> T): T = withContext(Dispatchers.IO) {
        try {
            block()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            throw SecureStoreException("Could not $action $key", exception)
        }
    }
}
