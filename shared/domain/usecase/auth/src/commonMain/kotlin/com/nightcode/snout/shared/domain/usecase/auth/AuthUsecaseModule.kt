package com.nightcode.snout.shared.domain.usecase.auth

import com.nightcode.snout.shared.data.auth.AuthDataModule
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [AuthDataModule::class])
class AuthUsecaseModule {
    @Single
    fun validateInstanceUrl() = ValidateInstanceUrl()

    @Single
    fun validatePersonalApiKey() = ValidatePersonalApiKey()

    @Single
    fun getPersonalApiKey(repository: AuthRepository) = GetPersonalApiKey(repository)

    @Single
    fun savePersonalApiKey(repository: AuthRepository) = SavePersonalApiKey(repository)

    @Single
    fun isPersonalApiKeySaved(repository: AuthRepository) = IsPersonalApiKeySaved(repository)
}
