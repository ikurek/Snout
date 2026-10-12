package com.nightcode.snout.shared.domain.usecase.featureflags

import com.nightcode.snout.shared.domain.data.featureflags.FeatureFlagRepository
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class FeatureFlagsUsecaseModule {
    @Single
    fun provideObserveFeatureFlags(observe: ObserveSelectedProjectId, repository: FeatureFlagRepository) =
        ObserveFeatureFlags(observe, repository)

    @Single
    fun provideRefreshFeatureFlags(get: GetSelectedProjectId, repository: FeatureFlagRepository) =
        RefreshFeatureFlags(get, repository)

    @Single
    fun provideSearchFeatureFlags(get: GetSelectedProjectId, repository: FeatureFlagRepository) =
        SearchFeatureFlags(get, repository)

    @Single
    fun provideGetFeatureFlag(get: GetSelectedProjectId, repository: FeatureFlagRepository) =
        GetFeatureFlag(get, repository)

    @Single
    fun provideSetFeatureFlagActive(get: GetSelectedProjectId, repository: FeatureFlagRepository) =
        SetFeatureFlagActive(get, repository)
}
