package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.data.appstate.AppStateDataModule
import com.nightcode.snout.shared.data.auth.AuthDataModule
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [AppStateDataModule::class, AuthDataModule::class])
@Suppress("TooManyFunctions") // one factory per use case; the module is a flat list by design
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

    @Single
    fun observeThemeMode(repository: AppStateRepository) = ObserveThemeMode(repository)

    @Single
    fun saveThemeMode(repository: AppStateRepository) = SaveThemeMode(repository)

    @Single
    fun getSelectedProjectId(repository: AppStateRepository) = GetSelectedProjectId(repository)

    @Single
    fun observeSelectedProjectId(repository: AppStateRepository) = ObserveSelectedProjectId(repository)

    @Single
    fun getPostHogUrl(authRepository: AuthRepository, appStateRepository: AppStateRepository) =
        GetPostHogUrl(authRepository, appStateRepository)

    @Single
    fun parsePostHogLink() = ParsePostHogLink()

    @Single
    fun observeNavigationPreferences(repository: AppStateRepository) = ObserveNavigationPreferences(repository)

    @Single
    fun saveNavigationPreferences(repository: AppStateRepository) = SaveNavigationPreferences(repository)

    @Single
    fun observeAnalyticsPreferences(repository: AppStateRepository) = ObserveAnalyticsPreferences(repository)

    @Single
    fun saveAnalyticsPreferences(repository: AppStateRepository) = SaveAnalyticsPreferences(repository)
}
