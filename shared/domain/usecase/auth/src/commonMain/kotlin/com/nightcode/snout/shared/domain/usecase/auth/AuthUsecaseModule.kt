package com.nightcode.snout.shared.domain.usecase.auth

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.appstate.CacheRepository
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import com.nightcode.snout.shared.domain.data.user.UserRepository
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class AuthUsecaseModule {
    @Single
    fun provideValidateInstanceUrl() = ValidateInstanceUrl()

    @Single
    fun provideValidatePersonalApiKey() = ValidatePersonalApiKey()

    @Single
    fun provideValidateUserAuth(authRepository: AuthRepository, userRepository: UserRepository) =
        ValidateAndSaveUserAuth(authRepository, userRepository)

    @Single
    fun provideClearLocalData(
        appStateRepository: AppStateRepository,
        organizationRepository: OrganizationRepository,
        cacheRepository: CacheRepository,
    ) = ClearLocalData(appStateRepository, organizationRepository, cacheRepository)

    @Single
    fun provideLogout(repository: AuthRepository, clearLocalData: ClearLocalData) = Logout(repository, clearLocalData)

    @Single
    fun provideGetInstanceUrl(repository: AuthRepository) = GetInstanceUrl(repository)

    @Single
    fun provideShouldReturnToLogin(repository: AuthRepository) = ShouldReturnToLogin(repository)
}
