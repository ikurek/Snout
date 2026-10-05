package com.nightcode.snout.shared.domain.usecase.auth

import com.nightcode.snout.shared.data.appstate.AppStateDataModule
import com.nightcode.snout.shared.data.auth.AuthDataModule
import com.nightcode.snout.shared.data.common.DataCommonModule
import com.nightcode.snout.shared.data.organization.OrganizationDataModule
import com.nightcode.snout.shared.data.user.UserDataModule
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.appstate.CacheRepository
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import com.nightcode.snout.shared.domain.data.user.UserRepository
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(
    includes = [
        AuthDataModule::class,
        UserDataModule::class,
        AppStateDataModule::class,
        OrganizationDataModule::class,
        DataCommonModule::class,
    ],
)
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
    fun clearLocalData(
        appStateRepository: AppStateRepository,
        organizationRepository: OrganizationRepository,
        cacheRepository: CacheRepository,
    ) = ClearLocalData(appStateRepository, organizationRepository, cacheRepository)

    @Single
    fun logout(repository: AuthRepository, clearLocalData: ClearLocalData) = Logout(repository, clearLocalData)

    @Single
    fun getInstanceUrl(repository: AuthRepository) = GetInstanceUrl(repository)

    @Single
    fun shouldReturnToLogin(repository: AuthRepository) = ShouldReturnToLogin(repository)
}
