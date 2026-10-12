package com.nightcode.snout.shared.data.user

import com.nightcode.snout.shared.api.users.UsersApiModule
import com.nightcode.snout.shared.storage.securestore.SecureStoreModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [UsersApiModule::class, SecureStoreModule::class])
@ComponentScan("com.nightcode.snout.shared.data.user")
class UserDataModule
