package com.nightcode.snout.shared.domain.usecase.analytics

import com.nightcode.snout.shared.domain.data.analytics.AnalyticsRepository
import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class AnalyticsUsecaseModule {
    @Single
    fun provideObserveAnalytics(observeSelectedProjectId: ObserveSelectedProjectId, repository: AnalyticsRepository) =
        ObserveAnalytics(observeSelectedProjectId, repository)

    @Single
    fun provideRefreshAnalytics(getSelectedProjectId: GetSelectedProjectId, repository: AnalyticsRepository) =
        RefreshAnalytics(getSelectedProjectId, repository)

    @Single
    fun provideObserveAnalyticsPreferences(repository: AppStateRepository) = ObserveAnalyticsPreferences(repository)

    @Single
    fun provideSaveAnalyticsPreferences(repository: AppStateRepository) = SaveAnalyticsPreferences(repository)
}
