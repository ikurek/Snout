package com.nightcode.snout.shared.data.experiments

import arrow.core.Either
import com.nightcode.snout.shared.api.experiments.ExperimentsApi
import com.nightcode.snout.shared.data.common.toDataError
import com.nightcode.snout.shared.data.experiments.mapper.toExperiment
import com.nightcode.snout.shared.domain.data.experiments.ExperimentRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.experiments.Experiment
import org.koin.core.annotation.Single

@Single(binds = [ExperimentRepository::class])
internal class ExperimentRepositoryImpl(private val api: ExperimentsApi) : ExperimentRepository {

    override suspend fun getExperiments(projectId: Long): Either<DataError, List<Experiment>> =
        api.getExperiments(projectId)
            .mapLeft { it.toDataError() }
            .map { response -> response.results.filterNot { it.deleted }.map { it.toExperiment() } }

    override suspend fun getExperiment(projectId: Long, experimentId: Long): Either<DataError, Experiment> =
        api.getExperiment(projectId, experimentId).mapLeft { it.toDataError() }.map { it.toExperiment() }
}
