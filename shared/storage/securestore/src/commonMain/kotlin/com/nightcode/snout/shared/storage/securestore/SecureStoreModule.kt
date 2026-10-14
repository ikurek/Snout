package com.nightcode.snout.shared.storage.securestore

import org.koin.core.annotation.Module

@Module(includes = [PlatformSecureStoreModule::class])
class SecureStoreModule

@Module
internal expect class PlatformSecureStoreModule()
