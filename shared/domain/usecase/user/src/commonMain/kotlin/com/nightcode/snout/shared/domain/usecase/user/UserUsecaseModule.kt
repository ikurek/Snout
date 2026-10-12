package com.nightcode.snout.shared.domain.usecase.user

import com.nightcode.snout.shared.domain.data.user.UserRepository
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class UserUsecaseModule {
    @Single
    fun provideGetCurrentUser(repository: UserRepository) = GetCurrentUser(repository)
}
