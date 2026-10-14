package com.nightcode.snout.shared.storage.database

import app.cash.sqldelight.db.SqlDriver
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [PlatformDatabaseModule::class])
class DatabaseModule {
    @Single
    internal fun provideSnoutDatabase(sqlDriver: SqlDriver): SnoutDatabase = SnoutDatabase(sqlDriver)

    @Single
    internal fun provideOrganizationsDatabase(database: SnoutDatabase): OrganizationsDatabase =
        OrganizationsDatabase(database)
}

/** Provides the platform's [SqlDriver] for the app's database file. */
@Module
internal expect class PlatformDatabaseModule()
