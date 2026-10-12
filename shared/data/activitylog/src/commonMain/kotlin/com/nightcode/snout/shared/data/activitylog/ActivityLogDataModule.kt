package com.nightcode.snout.shared.data.activitylog

import com.nightcode.snout.shared.api.activitylog.ActivityLogApiModule
import com.nightcode.snout.shared.data.common.CommonDataModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [ActivityLogApiModule::class, CommonDataModule::class])
@ComponentScan("com.nightcode.snout.shared.data.activitylog")
class ActivityLogDataModule
