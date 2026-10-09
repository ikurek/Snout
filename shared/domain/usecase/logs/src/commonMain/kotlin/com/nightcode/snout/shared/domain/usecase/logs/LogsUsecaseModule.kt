package com.nightcode.snout.shared.domain.usecase.logs

import com.nightcode.snout.shared.data.logs.LogsDataModule
import com.nightcode.snout.shared.domain.data.logs.LogsRepository
import com.nightcode.snout.shared.domain.usecase.appstate.AppStateUsecaseModule
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [LogsDataModule::class, AppStateUsecaseModule::class])
class LogsUsecaseModule {
    @Single
    fun observeLogServices(observe: ObserveSelectedProjectId, repository: LogsRepository) =
        ObserveLogServices(observe, repository)

    @Single
    fun refreshLogServices(get: GetSelectedProjectId, repository: LogsRepository) = RefreshLogServices(get, repository)

    @Single
    fun getServiceLogs(get: GetSelectedProjectId, repository: LogsRepository) = GetServiceLogs(get, repository)

    @Single
    fun getLogEntry(get: GetSelectedProjectId, repository: LogsRepository) = GetLogEntry(get, repository)

    @Single
    fun getServiceLogVolume(get: GetSelectedProjectId, repository: LogsRepository) =
        GetServiceLogVolume(get, repository)
}
