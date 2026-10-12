package com.nightcode.snout.shared.data.featureflags

import arrow.core.Either
import com.nightcode.snout.shared.api.featureflags.FeatureFlagsApi
import com.nightcode.snout.shared.data.common.JsonCacheStore
import com.nightcode.snout.shared.data.common.toDataError
import com.nightcode.snout.shared.data.featureflags.mapper.toFeatureFlag
import com.nightcode.snout.shared.domain.data.featureflags.FeatureFlagRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.featureflags.FeatureFlag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Single

@Single(binds = [FeatureFlagRepository::class])
internal class FeatureFlagRepositoryImpl(private val api: FeatureFlagsApi, private val cache: JsonCacheStore) :
    FeatureFlagRepository {

    override fun observeFeatureFlags(projectId: Long): Flow<List<FeatureFlag>?> = cache.observe(key(projectId))

    override suspend fun refreshFeatureFlags(projectId: Long): Either<DataError, Unit> = api.getFeatureFlags(projectId)
        .mapLeft { it.toDataError() }
        .map { response ->
            cache.put(
                key(projectId),
                response.results.filterNot { it.deleted }.map { it.toFeatureFlag() },
            )
        }

    override suspend fun searchFeatureFlags(projectId: Long, query: String): Either<DataError, List<FeatureFlag>> =
        api.getFeatureFlags(projectId, search = query)
            .mapLeft { it.toDataError() }
            .map { response -> response.results.filterNot { it.deleted }.map { it.toFeatureFlag() } }

    override suspend fun getFeatureFlag(projectId: Long, flagId: Long): Either<DataError, FeatureFlag> =
        api.getFeatureFlag(projectId, flagId).mapLeft { it.toDataError() }.map { it.toFeatureFlag() }

    override suspend fun setActive(projectId: Long, flagId: Long, active: Boolean): Either<DataError, FeatureFlag> =
        api.setActive(projectId, flagId, active)
            .mapLeft { it.toDataError() }
            .map { it.toFeatureFlag() }
            .onRight { updated ->
                val cached = cache.observe<List<FeatureFlag>>(key(projectId)).first() ?: return@onRight
                cache.put(key(projectId), cached.map { if (it.id == updated.id) updated else it })
            }

    private fun key(projectId: Long) = "feature_flags:$projectId"
}
