package com.nightcode.snout.shared.data.common

import com.nightcode.snout.shared.api.ApiModule
import com.nightcode.snout.shared.storage.database.DatabaseModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [ApiModule::class, DatabaseModule::class])
@ComponentScan("com.nightcode.snout.shared.data.common")
class CommonDataModule
