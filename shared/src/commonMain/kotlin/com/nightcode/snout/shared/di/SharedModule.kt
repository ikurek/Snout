package com.nightcode.snout.shared.di

import com.nightcode.snout.shared.domain.usecase.auth.AuthUsecaseModule
import org.koin.core.annotation.Module

@Module(
    includes = [
        AuthUsecaseModule::class,
    ],
)
class SharedModule
