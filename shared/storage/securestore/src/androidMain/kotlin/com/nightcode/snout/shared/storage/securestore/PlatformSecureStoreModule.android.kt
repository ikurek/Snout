package com.nightcode.snout.shared.storage.securestore

import android.content.Context
import eu.anifantakis.lib.ksafe.KSafe
import eu.anifantakis.lib.ksafe.KSafeMemoryPolicy
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
internal actual class PlatformSecureStoreModule {
    @Single(binds = [SecureStore::class])
    fun provideSecureStore(context: Context): SecureStore =
        KSafeSecureStore(KSafe(context, fileName = KSafeFileName, memoryPolicy = KSafeMemoryPolicy.ENCRYPTED))
}
