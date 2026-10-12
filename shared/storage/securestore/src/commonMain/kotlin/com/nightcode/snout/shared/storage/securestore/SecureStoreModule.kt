package com.nightcode.snout.shared.storage.securestore

import com.nightcode.snout.shared.common.coroutines.CoroutineDispatchersModule
import org.koin.core.annotation.Module

@Module(includes = [PlatformSecureStoreModule::class, CoroutineDispatchersModule::class])
class SecureStoreModule

@Module
internal expect class PlatformSecureStoreModule()
