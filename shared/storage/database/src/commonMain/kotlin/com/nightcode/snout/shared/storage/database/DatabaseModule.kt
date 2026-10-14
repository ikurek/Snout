package com.nightcode.snout.shared.storage.database

import app.cash.sqldelight.db.SqlDriver
import com.nightcode.snout.shared.common.coroutines.CoroutineDispatchersModule
import com.nightcode.snout.shared.common.coroutines.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [PlatformDatabaseModule::class, CoroutineDispatchersModule::class])
class DatabaseModule {
    @Single
    internal fun provideSnoutDatabase(sqlDriver: SqlDriver): SnoutDatabase = SnoutDatabase(sqlDriver)

    @Single
    internal fun provideOrganizationsDatabase(
        database: SnoutDatabase,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): OrganizationsDatabase = OrganizationsDatabase(database, ioDispatcher)
}

/** Provides the platform's [SqlDriver] for the app's database file. */
@Module
internal expect class PlatformDatabaseModule()
