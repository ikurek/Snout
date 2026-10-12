package com.nightcode.snout.shared.data.experiments

import arrow.core.Either
import com.nightcode.snout.shared.api.experiments.ExperimentsApi
import com.nightcode.snout.shared.data.common.JsonCacheStore
import com.nightcode.snout.shared.data.common.toDataError
import com.nightcode.snout.shared.data.experiments.mapper.toExperiment
import com.nightcode.snout.shared.domain.data.experiments.ExperimentRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.experiments.Experiment
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single(binds = [ExperimentRepository::class])
internal class ExperimentRepositoryImpl(private val api: ExperimentsApi, private val cache: JsonCacheStore) :
    ExperimentRepository {

    override fun observeExperiments(projectId: Long): Flow<List<Experiment>?> = cache.observe(key(projectId))

    override suspend fun refreshExperiments(projectId: Long): Either<DataError, Unit> = api.getExperiments(projectId)
        .mapLeft { it.toDataError() }
        .map { response ->
            cache.put(
                key(projectId),
                response.results.filterNot { it.deleted }.map { it.toExperiment() },
            )
        }

    override suspend fun getExperiment(projectId: Long, experimentId: Long): Either<DataError, Experiment> =
        api.getExperiment(projectId, experimentId).mapLeft { it.toDataError() }.map { it.toExperiment() }

    private fun key(projectId: Long) = "experiments:$projectId"
}
