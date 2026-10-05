package com.nightcode.snout.shared.domain.usecase.featureflags

import com.nightcode.snout.shared.data.featureflags.FeatureFlagsDataModule
import com.nightcode.snout.shared.domain.data.featureflags.FeatureFlagRepository
import com.nightcode.snout.shared.domain.usecase.appstate.AppStateUsecaseModule
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [FeatureFlagsDataModule::class, AppStateUsecaseModule::class])
class FeatureFlagsUsecaseModule {
    @Single
    fun observeFeatureFlags(observe: ObserveSelectedProjectId, repository: FeatureFlagRepository) =
        ObserveFeatureFlags(observe, repository)

    @Single
    fun refreshFeatureFlags(get: GetSelectedProjectId, repository: FeatureFlagRepository) =
        RefreshFeatureFlags(get, repository)

    @Single
    fun searchFeatureFlags(get: GetSelectedProjectId, repository: FeatureFlagRepository) =
        SearchFeatureFlags(get, repository)

    @Single
    fun getFeatureFlag(get: GetSelectedProjectId, repository: FeatureFlagRepository) = GetFeatureFlag(get, repository)

    @Single
    fun setFeatureFlagActive(get: GetSelectedProjectId, repository: FeatureFlagRepository) =
        SetFeatureFlagActive(get, repository)
}
