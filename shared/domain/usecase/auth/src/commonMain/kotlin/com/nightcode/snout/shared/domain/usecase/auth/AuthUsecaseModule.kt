package com.nightcode.snout.shared.domain.usecase.auth

import com.nightcode.snout.shared.data.auth.AuthDataModule
import com.nightcode.snout.shared.data.user.UserDataModule
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.data.user.UserRepository
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [AuthDataModule::class, UserDataModule::class])
class AuthUsecaseModule {
    @Single
    fun validateInstanceUrl() = ValidateInstanceUrl()

    @Single
    fun validatePersonalApiKey() = ValidatePersonalApiKey()

    @Single
    fun isPersonalApiKeySaved(repository: AuthRepository) = IsPersonalApiKeySaved(repository)

    @Single
    fun validateUserAuth(authRepository: AuthRepository, userRepository: UserRepository) =
        ValidateAndSaveUserAuth(authRepository, userRepository)

    @Single
    fun logout(repository: AuthRepository) = Logout(repository)

    @Single
    fun shouldReturnToLogin(repository: AuthRepository) = ShouldReturnToLogin(repository)
}
