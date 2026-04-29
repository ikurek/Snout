package com.nightcode.snout.shared.storage.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

internal const val PreferencesFileName = "snout.preferences_pb"

@Module(includes = [PlatformDataStoreModule::class])
class DataStoreModule {

    @Single
    fun authDataStore(dateStore: DataStore<Preferences>) = AuthDataStore(dateStore)
}

@Module
expect class PlatformDataStoreModule()
