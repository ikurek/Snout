package com.nightcode.snout.shared.domain.usecase.featureflags

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.featureflags.FeatureFlagRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.featureflags.FeatureFlag
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import org.koin.core.annotation.Single

@Single
class SearchFeatureFlags(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: FeatureFlagRepository,
) {
    suspend operator fun invoke(query: String): Either<DataError, List<FeatureFlag>> =
        repository.searchFeatureFlags(getSelectedProjectId(), query)
}
