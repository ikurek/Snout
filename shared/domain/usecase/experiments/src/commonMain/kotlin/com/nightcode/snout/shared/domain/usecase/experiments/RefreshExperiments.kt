package com.nightcode.snout.shared.domain.usecase.experiments

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.experiments.ExperimentRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import org.koin.core.annotation.Single

@Single
class RefreshExperiments(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: ExperimentRepository,
) {
    suspend operator fun invoke(): Either<DataError, Unit> = repository.refreshExperiments(getSelectedProjectId())
}
