package com.nightcode.snout.shared.domain.data.featureflags

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.featureflags.FeatureFlag
import kotlinx.coroutines.flow.Flow

interface FeatureFlagRepository {
    /** Emits after each successful change made through this app, so lists showing the old state load again. */
    val changes: Flow<Unit>

    suspend fun getFeatureFlags(projectId: Long): Either<DataError, List<FeatureFlag>>
    suspend fun searchFeatureFlags(projectId: Long, query: String): Either<DataError, List<FeatureFlag>>
    suspend fun getFeatureFlag(projectId: Long, flagId: Long): Either<DataError, FeatureFlag>
    suspend fun setActive(projectId: Long, flagId: Long, active: Boolean): Either<DataError, FeatureFlag>
}
