package com.nightcode.snout.shared.data.featureflags

import com.nightcode.snout.shared.api.featureflags.FeatureFlagsApiModule
import com.nightcode.snout.shared.data.common.CommonDataModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [FeatureFlagsApiModule::class, CommonDataModule::class])
@ComponentScan("com.nightcode.snout.shared.data.featureflags")
class FeatureFlagsDataModule
