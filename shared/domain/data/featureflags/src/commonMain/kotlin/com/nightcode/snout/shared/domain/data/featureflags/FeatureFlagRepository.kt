package com.nightcode.snout.shared.domain.data.featureflags

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.featureflags.FeatureFlag
import kotlinx.coroutines.flow.Flow

interface FeatureFlagRepository {
    fun observeFeatureFlags(projectId: Long): Flow<List<FeatureFlag>?>
    suspend fun refreshFeatureFlags(projectId: Long): Either<DataError, Unit>
    suspend fun searchFeatureFlags(projectId: Long, query: String): Either<DataError, List<FeatureFlag>>
    suspend fun getFeatureFlag(projectId: Long, flagId: Long): Either<DataError, FeatureFlag>
    suspend fun setActive(projectId: Long, flagId: Long, active: Boolean): Either<DataError, FeatureFlag>
}
