package com.nightcode.snout.shared.domain.usecase.experiments

import com.nightcode.snout.shared.domain.data.experiments.ExperimentRepository
import com.nightcode.snout.shared.domain.model.experiments.Experiment
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class ObserveExperiments(
    private val observeSelectedProjectId: ObserveSelectedProjectId,
    private val repository: ExperimentRepository,
) {
    operator fun invoke(): Flow<List<Experiment>?> =
        observeSelectedProjectId.flatMapProject { repository.observeExperiments(it) }
}
