package com.nightcode.snout.shared.storage.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import platform.Foundation.NSBundle

@Module
internal actual class PlatformDatabaseModule {
    /**
     * The app's database file. The widget extension's lives in memory only: a file would sit in the extension's own
     * container, which logging out in the app can't clear, keeping the account's organizations and projects.
     */
    @Single
    fun provideSqlDriver(): SqlDriver = NativeSqliteDriver(
        schema = SnoutDatabase.Schema,
        name = "snout.db",
        onConfiguration = { it.copy(inMemory = NSBundle.mainBundle.bundlePath.endsWith(".appex")) },
    )
}
