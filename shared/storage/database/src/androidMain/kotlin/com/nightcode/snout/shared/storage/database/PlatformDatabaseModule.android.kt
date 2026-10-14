package com.nightcode.snout.shared.storage.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
internal actual class PlatformDatabaseModule {
    @Single
    fun provideSqlDriver(context: Context): SqlDriver = AndroidSqliteDriver(SnoutDatabase.Schema, context, "snout.db")
}
