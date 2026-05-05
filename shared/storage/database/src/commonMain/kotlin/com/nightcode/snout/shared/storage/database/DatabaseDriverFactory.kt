package com.nightcode.snout.shared.storage.database

import app.cash.sqldelight.db.SqlDriver

internal interface DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
