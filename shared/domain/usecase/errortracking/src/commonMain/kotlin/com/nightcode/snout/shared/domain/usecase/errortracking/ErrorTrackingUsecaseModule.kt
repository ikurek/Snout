package com.nightcode.snout.shared.domain.usecase.errortracking

import com.nightcode.snout.shared.data.errortracking.ErrorTrackingDataModule
import com.nightcode.snout.shared.domain.data.errortracking.ErrorTrackingRepository
import com.nightcode.snout.shared.domain.usecase.appstate.AppStateUsecaseModule
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [ErrorTrackingDataModule::class, AppStateUsecaseModule::class])
class ErrorTrackingUsecaseModule {
    @Single
    fun observeErrorTrackingIssues(observe: ObserveSelectedProjectId, repository: ErrorTrackingRepository) =
        ObserveErrorTrackingIssues(observe, repository)

    @Single
    fun refreshErrorTrackingIssues(get: GetSelectedProjectId, repository: ErrorTrackingRepository) =
        RefreshErrorTrackingIssues(get, repository)

    @Single
    fun searchErrorTrackingIssues(get: GetSelectedProjectId, repository: ErrorTrackingRepository) =
        SearchErrorTrackingIssues(get, repository)

    @Single
    fun getErrorTrackingIssue(get: GetSelectedProjectId, repository: ErrorTrackingRepository) =
        GetErrorTrackingIssue(get, repository)

    @Single
    fun getErrorAppVersions(get: GetSelectedProjectId, repository: ErrorTrackingRepository) =
        GetErrorAppVersions(get, repository)
}
