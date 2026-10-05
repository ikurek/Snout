package com.nightcode.snout.shared.domain.usecase.analytics

import com.nightcode.snout.shared.data.analytics.AnalyticsDataModule
import com.nightcode.snout.shared.domain.data.analytics.AnalyticsRepository
import com.nightcode.snout.shared.domain.usecase.appstate.AppStateUsecaseModule
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [AnalyticsDataModule::class, AppStateUsecaseModule::class])
class AnalyticsUsecaseModule {
    @Single
    fun observeAnalytics(observeSelectedProjectId: ObserveSelectedProjectId, repository: AnalyticsRepository) =
        ObserveAnalytics(observeSelectedProjectId, repository)

    @Single
    fun refreshAnalytics(getSelectedProjectId: GetSelectedProjectId, repository: AnalyticsRepository) =
        RefreshAnalytics(getSelectedProjectId, repository)
}
