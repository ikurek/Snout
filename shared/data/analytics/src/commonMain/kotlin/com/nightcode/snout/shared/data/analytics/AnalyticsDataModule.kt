package com.nightcode.snout.shared.data.analytics

import com.nightcode.snout.shared.api.analytics.AnalyticsApiModule
import com.nightcode.snout.shared.data.common.CommonDataModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [AnalyticsApiModule::class, CommonDataModule::class])
@ComponentScan("com.nightcode.snout.shared.data.analytics")
class AnalyticsDataModule
