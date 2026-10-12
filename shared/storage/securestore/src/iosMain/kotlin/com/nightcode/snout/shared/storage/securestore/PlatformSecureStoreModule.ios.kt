package com.nightcode.snout.shared.storage.securestore

import com.nightcode.snout.shared.common.coroutines.IoDispatcher
import eu.anifantakis.lib.ksafe.KSafe
import eu.anifantakis.lib.ksafe.KSafeMemoryPolicy
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
internal actual class PlatformSecureStoreModule {
    @Single(binds = [SecureStore::class])
    fun provideSecureStore(@IoDispatcher ioDispatcher: CoroutineDispatcher): SecureStore = KSafeSecureStore(
        ksafe = KSafe(fileName = KSafeFileName, memoryPolicy = KSafeMemoryPolicy.ENCRYPTED),
        ioDispatcher = ioDispatcher,
    )
}
