package com.nightcode.snout.shared.data.experiments

import com.nightcode.snout.shared.api.experiments.ExperimentsApiModule
import com.nightcode.snout.shared.data.common.CommonDataModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [ExperimentsApiModule::class, CommonDataModule::class])
@ComponentScan("com.nightcode.snout.shared.data.experiments")
class ExperimentsDataModule
