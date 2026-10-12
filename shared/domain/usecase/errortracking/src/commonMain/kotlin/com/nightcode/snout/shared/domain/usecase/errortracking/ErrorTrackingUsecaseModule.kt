package com.nightcode.snout.shared.domain.usecase.errortracking

import com.nightcode.snout.shared.domain.data.errortracking.ErrorTrackingRepository
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class ErrorTrackingUsecaseModule {
    @Single
    fun provideObserveErrorTrackingIssues(observe: ObserveSelectedProjectId, repository: ErrorTrackingRepository) =
        ObserveErrorTrackingIssues(observe, repository)

    @Single
    fun provideRefreshErrorTrackingIssues(get: GetSelectedProjectId, repository: ErrorTrackingRepository) =
        RefreshErrorTrackingIssues(get, repository)

    @Single
    fun provideSearchErrorTrackingIssues(get: GetSelectedProjectId, repository: ErrorTrackingRepository) =
        SearchErrorTrackingIssues(get, repository)

    @Single
    fun provideGetErrorTrackingIssue(get: GetSelectedProjectId, repository: ErrorTrackingRepository) =
        GetErrorTrackingIssue(get, repository)

    @Single
    fun provideGetErrorAppVersions(get: GetSelectedProjectId, repository: ErrorTrackingRepository) =
        GetErrorAppVersions(get, repository)

    @Single
    fun provideSetErrorTrackingIssueStatus(get: GetSelectedProjectId, repository: ErrorTrackingRepository) =
        SetErrorTrackingIssueStatus(get, repository)
}
