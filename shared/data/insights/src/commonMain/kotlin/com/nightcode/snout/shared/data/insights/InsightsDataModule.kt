package com.nightcode.snout.shared.data.insights

import com.nightcode.snout.shared.api.insights.InsightsApiModule
import com.nightcode.snout.shared.data.common.CommonDataModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [InsightsApiModule::class, CommonDataModule::class])
@ComponentScan("com.nightcode.snout.shared.data.insights")
class InsightsDataModule
