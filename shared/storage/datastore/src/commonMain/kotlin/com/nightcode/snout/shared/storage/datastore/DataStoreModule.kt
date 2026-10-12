package com.nightcode.snout.shared.storage.datastore

import com.nightcode.snout.shared.storage.securestore.SecureStoreModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

internal const val PreferencesFileName = "snout.preferences_pb"

@Module(includes = [PlatformDataStoreModule::class, SecureStoreModule::class])
@ComponentScan("com.nightcode.snout.shared.storage.datastore")
class DataStoreModule

@Module
expect class PlatformDataStoreModule()
