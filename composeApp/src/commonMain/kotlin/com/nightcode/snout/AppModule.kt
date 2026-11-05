package com.nightcode.snout

import com.nightcode.snout.shared.di.SharedModule
import com.nightcode.snout.widgets.WidgetsModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(
    includes = [
        SharedModule::class,
        WidgetsModule::class,
    ],
)
@ComponentScan
class AppModule
