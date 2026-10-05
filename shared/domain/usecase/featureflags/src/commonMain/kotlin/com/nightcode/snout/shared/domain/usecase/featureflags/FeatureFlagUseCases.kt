package com.nightcode.snout.shared.domain.usecase.featureflags

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.featureflags.FeatureFlagRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.featureflags.FeatureFlag
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveFeatureFlags(
    private val observeSelectedProjectId: ObserveSelectedProjectId,
    private val repository: FeatureFlagRepository,
) {
    operator fun invoke(): Flow<List<FeatureFlag>?> =
        observeSelectedProjectId().flatMapLatest { repository.observeFlags(it) }
}

class RefreshFeatureFlags(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: FeatureFlagRepository,
) {
    suspend operator fun invoke(): Either<DataError, Unit> = repository.refreshFlags(getSelectedProjectId())
}

class SearchFeatureFlags(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: FeatureFlagRepository,
) {
    suspend operator fun invoke(query: String): Either<DataError, List<FeatureFlag>> =
        repository.searchFlags(getSelectedProjectId(), query)
}

class GetFeatureFlag(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: FeatureFlagRepository,
) {
    suspend operator fun invoke(flagId: Long): Either<DataError, FeatureFlag> =
        repository.getFlag(getSelectedProjectId(), flagId)
}

class SetFeatureFlagActive(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: FeatureFlagRepository,
) {
    suspend operator fun invoke(flagId: Long, active: Boolean): Either<DataError, FeatureFlag> =
        repository.setActive(getSelectedProjectId(), flagId, active)
}
