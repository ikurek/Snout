package com.nightcode.snout.shared.storage.database

import android.content.Context
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan
internal actual class DatabaseDriverFactoryModule

@Single
internal fun providesDatabaseDriverFactory(context: Context): DatabaseDriverFactory =
    AndroidDatabaseDriverFactory(context)
