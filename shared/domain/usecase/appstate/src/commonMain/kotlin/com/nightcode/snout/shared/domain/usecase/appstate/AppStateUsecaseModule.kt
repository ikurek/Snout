package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.data.appstate.AppStateDataModule
import com.nightcode.snout.shared.data.auth.AuthDataModule
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [AppStateDataModule::class, AuthDataModule::class])
class AppStateUsecaseModule {
    @Single
    fun saveSelectedProject(repository: AppStateRepository) = SaveSelectedProject(repository)

    @Single
    fun getSelectedProject(repository: AppStateRepository) = GetSelectedProject(repository)

    @Single
    fun determineInitialLocation(authRepository: AuthRepository, appStateRepository: AppStateRepository) =
        DetermineInitialLocation(authRepository, appStateRepository)

    @Single
    fun observeSelectedProject(repository: AppStateRepository) = ObserveSelectedProject(repository)
}
