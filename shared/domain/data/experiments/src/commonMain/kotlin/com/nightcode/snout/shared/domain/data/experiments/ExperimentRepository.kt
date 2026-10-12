package com.nightcode.snout.shared.domain.data.experiments

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.experiments.Experiment

interface ExperimentRepository {
    suspend fun getExperiments(projectId: Long): Either<DataError, List<Experiment>>
    suspend fun getExperiment(projectId: Long, experimentId: Long): Either<DataError, Experiment>
}
