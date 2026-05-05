package com.nightcode.snout.shared.storage.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

internal class IosDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver = NativeSqliteDriver(SnoutDatabase.Schema, "snout.db")
}
