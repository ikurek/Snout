package com.nightcode.snout.shared.storage.securestore

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * The app's [PersonalApiKeyStore] on iOS: one item in the Keychain group the app shares with its widget extension,
 * so both use the same key, and a change to it reaches both at once. It can't stay in KSafe with the rest of the
 * [SecureStore]: KSafe serves one process, caching what it read and writing its file at start.
 *
 * Versions before widgets kept the key in KSafe ([earlier]): the first read moves it over.
 */
internal class KeychainPersonalApiKeyStore(private val keychain: SharedKeychain, private val earlier: SecureStore) :
    PersonalApiKeyStore {

    override suspend fun get(): String? =
        keychain { it.get(PersonalApiKeyName) } ?: earlier.get(PersonalApiKeyName)?.also { moveToKeychain(it) }

    override suspend fun set(key: String) = keychain { it.set(PersonalApiKeyName, key) }

    override suspend fun clear() {
        keychain { it.remove(PersonalApiKeyName) }
        // A move that failed halfway may have left it in KSafe.
        earlier.remove(PersonalApiKeyName)
    }

    private suspend fun moveToKeychain(key: String) {
        keychain { it.set(PersonalApiKeyName, key) }
        earlier.remove(PersonalApiKeyName)
    }

    /** Keychain calls can wait on the Keychain daemon, so they run off the main thread. */
    private suspend fun <T> keychain(block: (SharedKeychain) -> T): T = withContext(Dispatchers.IO) { block(keychain) }
}

/**
 * The widget extension's [PersonalApiKeyStore]: the app's key, read from the shared Keychain item every time, so a
 * widget never uses a key the app has since replaced or removed. The extension never writes it: a 401 in a widget
 * leaves the key for the app to drop.
 */
internal class WidgetPersonalApiKeyStore(private val keychain: SharedKeychain) : PersonalApiKeyStore {

    override suspend fun get(): String? = withContext(Dispatchers.IO) { keychain.get(PersonalApiKeyName) }

    override suspend fun set(key: String) = Unit

    override suspend fun clear() = Unit
}

/**
 * The widget extension's [SecureStore], which holds nothing: what the app keeps in KSafe isn't shared, and opening
 * KSafe here would write to the app's file.
 */
internal object WidgetSecureStore : SecureStore {

    override suspend fun get(key: String): String? = null

    override suspend fun set(key: String, value: String) = Unit

    override suspend fun remove(key: String) = Unit
}
