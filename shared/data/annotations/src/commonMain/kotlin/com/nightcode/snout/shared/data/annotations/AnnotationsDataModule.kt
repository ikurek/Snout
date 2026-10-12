package com.nightcode.snout.shared.data.annotations

import com.nightcode.snout.shared.api.annotations.AnnotationsApiModule
import com.nightcode.snout.shared.data.common.CommonDataModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [AnnotationsApiModule::class, CommonDataModule::class])
@ComponentScan("com.nightcode.snout.shared.data.annotations")
class AnnotationsDataModule
