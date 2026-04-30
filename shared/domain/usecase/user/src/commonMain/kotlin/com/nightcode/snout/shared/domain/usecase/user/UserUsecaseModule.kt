package com.nightcode.snout.shared.domain.usecase.user

import com.nightcode.snout.shared.data.user.UserDataModule
import com.nightcode.snout.shared.domain.data.user.UserRepository
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [UserDataModule::class])
class UserUsecaseModule {
    @Single
    fun getCurrentUser(repository: UserRepository) = GetCurrentUser(repository)
}
