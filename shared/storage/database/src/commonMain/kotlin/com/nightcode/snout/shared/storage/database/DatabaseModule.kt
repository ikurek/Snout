package com.nightcode.snout.shared.storage.database

import app.cash.sqldelight.db.SqlDriver
import com.nightcode.snout.shared.common.coroutines.CoroutineDispatchersModule
import com.nightcode.snout.shared.common.coroutines.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [DatabaseDriverFactoryModule::class, CoroutineDispatchersModule::class])
class DatabaseModule {
    @Single
    internal fun provideSqlDriver(databaseDriverFactory: DatabaseDriverFactory): SqlDriver =
        databaseDriverFactory.createDriver()

    @Single
    internal fun provideSnoutDatabase(sqlDriver: SqlDriver): SnoutDatabase = SnoutDatabase(sqlDriver)

    @Single(binds = [JsonCache::class])
    internal fun provideJsonCache(
        database: SnoutDatabase,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): JsonCache = JsonCacheImpl(database, ioDispatcher)

    @Single(binds = [OrganizationsDatabase::class])
    internal fun provideOrganizationsDatabase(
        database: SnoutDatabase,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): OrganizationsDatabase = OrganizationsDatabaseImpl(database, ioDispatcher)
}

@Module
internal expect class DatabaseDriverFactoryModule()
