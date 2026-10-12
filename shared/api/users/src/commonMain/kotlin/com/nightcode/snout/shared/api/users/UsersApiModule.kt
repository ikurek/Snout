package com.nightcode.snout.shared.api.users

import com.nightcode.snout.shared.api.ApiModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [ApiModule::class])
@ComponentScan("com.nightcode.snout.shared.api.users")
class UsersApiModule
