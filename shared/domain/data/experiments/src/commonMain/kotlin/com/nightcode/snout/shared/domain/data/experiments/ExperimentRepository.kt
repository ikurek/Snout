package com.nightcode.snout.shared.domain.data.experiments

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.experiments.Experiment
import kotlinx.coroutines.flow.Flow

interface ExperimentRepository {
    fun observeExperiments(projectId: Long): Flow<List<Experiment>?>
    suspend fun refreshExperiments(projectId: Long): Either<DataError, Unit>
    suspend fun getExperiment(projectId: Long, experimentId: Long): Either<DataError, Experiment>
}
