package com.nightcode.snout.shared.data.errortracking

import com.nightcode.snout.shared.api.errortracking.ErrorTrackingApiModule
import com.nightcode.snout.shared.data.common.CommonDataModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [ErrorTrackingApiModule::class, CommonDataModule::class])
@ComponentScan("com.nightcode.snout.shared.data.errortracking")
class ErrorTrackingDataModule
