package com.nightcode.snout.shared.storage.database

import android.content.Context
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
internal actual class DatabaseDriverFactoryModule {
    @Single
    fun provideDatabaseDriverFactory(context: Context): DatabaseDriverFactory = AndroidDatabaseDriverFactory(context)
}
