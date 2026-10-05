package com.nightcode.snout.shared.domain.usecase.activitylog

import com.nightcode.snout.shared.data.activitylog.ActivityLogDataModule
import com.nightcode.snout.shared.domain.data.activitylog.ActivityLogRepository
import com.nightcode.snout.shared.domain.usecase.appstate.AppStateUsecaseModule
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [ActivityLogDataModule::class, AppStateUsecaseModule::class])
class ActivityLogUsecaseModule {
    @Single
    fun observeActivityLog(observe: ObserveSelectedProjectId, repository: ActivityLogRepository) =
        ObserveActivityLog(observe, repository)

    @Single
    fun refreshActivityLog(get: GetSelectedProjectId, repository: ActivityLogRepository) =
        RefreshActivityLog(get, repository)
}
