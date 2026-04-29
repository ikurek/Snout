package com.nightcode.snout.shared.storage.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toOkioPath
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
actual class PlatformDataStoreModule {
    @Single
    fun provideDataStore(context: Context): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath {
        context.filesDir.resolve(PreferencesFileName).toOkioPath()
    }
}
