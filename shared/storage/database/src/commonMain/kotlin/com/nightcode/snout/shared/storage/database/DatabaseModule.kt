package com.nightcode.snout.shared.storage.database

import app.cash.sqldelight.db.SqlDriver
import com.nightcode.snout.shared.common.coroutines.CoroutineDispatchersModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [DatabaseDriverFactoryModule::class, CoroutineDispatchersModule::class])
@ComponentScan
class DatabaseModule

internal expect class DatabaseDriverFactoryModule

@Single
internal fun provideSqlDriver(databaseDriverFactory: DatabaseDriverFactory): SqlDriver =
    databaseDriverFactory.createDriver()

@Single
internal fun provideSnoutDatabase(sqlDriver: SqlDriver): SnoutDatabase = SnoutDatabase(sqlDriver)
