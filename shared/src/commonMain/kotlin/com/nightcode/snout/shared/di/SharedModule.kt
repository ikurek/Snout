package com.nightcode.snout.shared.di

import com.nightcode.snout.shared.domain.usecase.auth.AuthUsecaseModule
import com.nightcode.snout.shared.domain.usecase.user.UserUsecaseModule
import org.koin.core.annotation.Module

@Module(
    includes = [
        AuthUsecaseModule::class,
        UserUsecaseModule::class,
    ],
)
class SharedModule
