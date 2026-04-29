package com.nightcode.snout

import com.nightcode.snout.shared.di.SharedModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [
    SharedModule::class
])
@ComponentScan
class AppModule
