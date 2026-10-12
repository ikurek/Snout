package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Suppress("TooManyFunctions") // one factory per use case; the module is a flat list by design
class AppStateUsecaseModule {
    @Single
    fun provideSaveSelectedProject(repository: AppStateRepository) = SaveSelectedProject(repository)

    @Single
    fun provideGetSelectedProject(repository: AppStateRepository) = GetSelectedProject(repository)

    @Single
    fun provideDetermineInitialLocation(authRepository: AuthRepository, appStateRepository: AppStateRepository) =
        DetermineInitialLocation(authRepository, appStateRepository)

    @Single
    fun provideObserveSelectedProject(repository: AppStateRepository) = ObserveSelectedProject(repository)

    @Single
    fun provideObserveThemeMode(repository: AppStateRepository) = ObserveThemeMode(repository)

    @Single
    fun provideSaveThemeMode(repository: AppStateRepository) = SaveThemeMode(repository)

    @Single
    fun provideGetSelectedProjectId(repository: AppStateRepository) = GetSelectedProjectId(repository)

    @Single
    fun provideObserveSelectedProjectId(repository: AppStateRepository) = ObserveSelectedProjectId(repository)

    @Single
    fun provideObserveNavigationPreferences(repository: AppStateRepository) = ObserveNavigationPreferences(repository)

    @Single
    fun provideSaveNavigationPreferences(repository: AppStateRepository) = SaveNavigationPreferences(repository)
}
