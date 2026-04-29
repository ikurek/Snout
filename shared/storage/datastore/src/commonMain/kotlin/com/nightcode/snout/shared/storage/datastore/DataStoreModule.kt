package com.nightcode.snout.shared.storage.datastore

import org.koin.core.annotation.Module

internal const val PreferencesFileName = "snout.preferences_pb"

@Module(includes = [PlatformDataStoreModule::class])
class DataStoreModule

@Module
expect class PlatformDataStoreModule()
