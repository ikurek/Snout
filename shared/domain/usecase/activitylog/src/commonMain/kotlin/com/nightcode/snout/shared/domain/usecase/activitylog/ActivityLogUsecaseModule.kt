package com.nightcode.snout.shared.domain.usecase.activitylog

import com.nightcode.snout.shared.domain.data.activitylog.ActivityLogRepository
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class ActivityLogUsecaseModule {
    @Single
    fun provideObserveActivityLog(observe: ObserveSelectedProjectId, repository: ActivityLogRepository) =
        ObserveActivityLog(observe, repository)

    @Single
    fun provideRefreshActivityLog(get: GetSelectedProjectId, repository: ActivityLogRepository) =
        RefreshActivityLog(get, repository)
}
