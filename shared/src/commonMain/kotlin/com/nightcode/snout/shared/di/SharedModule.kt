package com.nightcode.snout.shared.di

import com.nightcode.snout.shared.domain.usecase.appstate.AppStateUsecaseModule
import com.nightcode.snout.shared.domain.usecase.auth.AuthUsecaseModule
import com.nightcode.snout.shared.domain.usecase.organization.OrganizationUsecaseModule
import com.nightcode.snout.shared.domain.usecase.user.UserUsecaseModule
import org.koin.core.annotation.Module

@Module(
    includes = [
        AppStateUsecaseModule::class,
        AuthUsecaseModule::class,
        OrganizationUsecaseModule::class,
        UserUsecaseModule::class,
    ],
)
class SharedModule
