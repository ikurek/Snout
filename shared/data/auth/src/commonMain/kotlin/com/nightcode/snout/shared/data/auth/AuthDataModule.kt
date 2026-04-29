package com.nightcode.snout.shared.data.auth

import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import com.nightcode.snout.shared.storage.datastore.DataStoreModule
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [DataStoreModule::class])
class AuthDataModule {
    @Single(binds = [AuthRepository::class])
    fun provideAuthRepository(dataStore: AuthDataStore): AuthRepository = AuthRepositoryImpl(dataStore)
}
