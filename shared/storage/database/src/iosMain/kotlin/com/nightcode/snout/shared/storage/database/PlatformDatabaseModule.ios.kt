package com.nightcode.snout.shared.storage.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
internal actual class PlatformDatabaseModule {
    @Single
    fun provideSqlDriver(): SqlDriver = NativeSqliteDriver(SnoutDatabase.Schema, "snout.db")
}
