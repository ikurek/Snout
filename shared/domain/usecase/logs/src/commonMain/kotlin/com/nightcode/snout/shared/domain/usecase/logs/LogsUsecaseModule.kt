package com.nightcode.snout.shared.domain.usecase.logs

import com.nightcode.snout.shared.domain.data.logs.LogsRepository
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class LogsUsecaseModule {
    @Single
    fun provideObserveLogServices(observe: ObserveSelectedProjectId, repository: LogsRepository) =
        ObserveLogServices(observe, repository)

    @Single
    fun provideRefreshLogServices(get: GetSelectedProjectId, repository: LogsRepository) =
        RefreshLogServices(get, repository)

    @Single
    fun provideGetServiceLogs(get: GetSelectedProjectId, repository: LogsRepository) = GetServiceLogs(get, repository)

    @Single
    fun provideGetLogEntry(get: GetSelectedProjectId, repository: LogsRepository) = GetLogEntry(get, repository)

    @Single
    fun provideGetServiceLogVolume(get: GetSelectedProjectId, repository: LogsRepository) =
        GetServiceLogVolume(get, repository)
}
