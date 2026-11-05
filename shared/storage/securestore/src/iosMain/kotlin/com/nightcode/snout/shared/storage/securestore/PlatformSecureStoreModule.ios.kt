package com.nightcode.snout.shared.storage.securestore

import eu.anifantakis.lib.ksafe.KSafe
import eu.anifantakis.lib.ksafe.KSafeMemoryPolicy
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
internal actual class PlatformSecureStoreModule {
    /** KSafe in the app; nothing in the widget extension, which must not open the app's KSafe file. */
    @Single(binds = [SecureStore::class])
    fun provideSecureStore(): SecureStore = if (SharedWithWidgets.isAppExtension) {
        WidgetSecureStore
    } else {
        KSafeSecureStore(KSafe(fileName = KSafeFileName, memoryPolicy = KSafeMemoryPolicy.ENCRYPTED))
    }

    /** The key in the Keychain item the app shares with its widget extension (see [KeychainPersonalApiKeyStore]). */
    @Single(binds = [PersonalApiKeyStore::class])
    fun providePersonalApiKeyStore(secureStore: SecureStore): PersonalApiKeyStore {
        val keychain = SharedKeychain(SharedWithWidgets.keychainAccessGroup)
        return if (SharedWithWidgets.isAppExtension) {
            WidgetPersonalApiKeyStore(keychain)
        } else {
            KeychainPersonalApiKeyStore(keychain, earlier = secureStore)
        }
    }
}
