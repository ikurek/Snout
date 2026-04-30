package com.nightcode.snout.shared.data.user

import com.nightcode.snout.shared.api.users.UsersApi
import com.nightcode.snout.shared.api.users.UsersApiModule
import com.nightcode.snout.shared.domain.data.user.UserRepository
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [UsersApiModule::class])
class UserDataModule {
    @Single(binds = [UserRepository::class])
    fun provideUserRepository(api: UsersApi): UserRepository = UserRepositoryImpl(api)
}
