package com.nightcode.snout.shared.storage.database

import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
internal actual class DatabaseDriverFactoryModule {
    @Single
    fun provideDatabaseDriverFactory(): DatabaseDriverFactory = IosDatabaseDriverFactory()
}
