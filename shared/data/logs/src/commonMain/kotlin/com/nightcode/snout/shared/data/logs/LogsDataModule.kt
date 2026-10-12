package com.nightcode.snout.shared.data.logs

import com.nightcode.snout.shared.api.logs.LogsApiModule
import com.nightcode.snout.shared.data.common.CommonDataModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [LogsApiModule::class, CommonDataModule::class])
@ComponentScan("com.nightcode.snout.shared.data.logs")
class LogsDataModule
