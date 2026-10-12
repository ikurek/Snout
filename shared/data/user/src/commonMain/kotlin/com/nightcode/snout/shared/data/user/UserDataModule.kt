package com.nightcode.snout.shared.data.user

import com.nightcode.snout.shared.api.users.UsersApiModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [UsersApiModule::class])
@ComponentScan("com.nightcode.snout.shared.data.user")
class UserDataModule
