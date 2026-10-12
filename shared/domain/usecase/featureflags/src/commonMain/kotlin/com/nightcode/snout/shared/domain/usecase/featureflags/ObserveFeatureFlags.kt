package com.nightcode.snout.shared.domain.usecase.featureflags

import com.nightcode.snout.shared.domain.data.featureflags.FeatureFlagRepository
import com.nightcode.snout.shared.domain.model.featureflags.FeatureFlag
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class ObserveFeatureFlags(
    private val observeSelectedProjectId: ObserveSelectedProjectId,
    private val repository: FeatureFlagRepository,
) {
    operator fun invoke(): Flow<List<FeatureFlag>?> =
        observeSelectedProjectId.flatMapProject { repository.observeFeatureFlags(it) }
}
