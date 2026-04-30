package com.nightcode.snout.shared.data.appstate

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.storage.datastore.AppStateDataStore
import com.nightcode.snout.shared.storage.datastore.DataStoreModule
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [DataStoreModule::class])
class AppStateDataModule {
    @Single(binds = [AppStateRepository::class])
    fun provideAppStateRepository(dataStore: AppStateDataStore): AppStateRepository = AppStateRepositoryImpl(dataStore)
}
