package com.nightcode.snout.shared.api.featureflags

import com.nightcode.snout.shared.api.ApiModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [ApiModule::class])
@ComponentScan("com.nightcode.snout.shared.api.featureflags")
class FeatureFlagsApiModule
