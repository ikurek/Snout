package com.nightcode.snout.shared.data.appstate

import com.nightcode.snout.shared.storage.datastore.DataStoreModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [DataStoreModule::class])
@ComponentScan("com.nightcode.snout.shared.data.appstate")
class AppStateDataModule
