package com.nightcode.snout.shared.domain.usecase.featureflags

import com.nightcode.snout.shared.domain.data.featureflags.FeatureFlagRepository
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveProjectReloads
import com.nightcode.snout.shared.domain.usecase.appstate.ProjectReload
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

/** When to load feature flags: at once, when another project is selected, and after each change made in the app. */
@Single
class ObserveFeatureFlagReloads(
    private val observeProjectReloads: ObserveProjectReloads,
    private val repository: FeatureFlagRepository,
) {
    operator fun invoke(): Flow<ProjectReload> = observeProjectReloads(repository.changes)
}
