package com.nightcode.snout.shared.domain.usecase.experiments

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.experiments.ExperimentRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.experiments.Experiment
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveExperiments(
    private val observeSelectedProjectId: ObserveSelectedProjectId,
    private val repository: ExperimentRepository,
) {
    operator fun invoke(): Flow<List<Experiment>?> =
        observeSelectedProjectId().flatMapLatest { repository.observeExperiments(it) }
}

class RefreshExperiments(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: ExperimentRepository,
) {
    suspend operator fun invoke(): Either<DataError, Unit> = repository.refreshExperiments(getSelectedProjectId())
}

class GetExperiment(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: ExperimentRepository,
) {
    suspend operator fun invoke(experimentId: Long): Either<DataError, Experiment> =
        repository.getExperiment(getSelectedProjectId(), experimentId)
}
