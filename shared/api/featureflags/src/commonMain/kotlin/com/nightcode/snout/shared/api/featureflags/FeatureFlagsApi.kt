package com.nightcode.snout.shared.api.featureflags

import com.nightcode.snout.shared.api.PaginatedResponseDto
import com.nightcode.snout.shared.api.featureflags.model.FeatureFlagDto
import com.nightcode.snout.shared.api.networkCall
import com.nightcode.snout.shared.api.patchJson
import com.nightcode.snout.shared.api.projectUrl
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val PageSize = 100

class FeatureFlagsApi internal constructor(private val client: HttpClient, private val authDataStore: AuthDataStore) {

    // Limitation: first 100 flags only; add offset paging when a project outgrows that.
    suspend fun getFeatureFlags(projectId: Long, search: String? = null) =
        networkCall<PaginatedResponseDto<FeatureFlagDto>> {
            client.get(authDataStore.projectUrl(projectId, "feature_flags/")) {
                parameter("limit", PageSize)
                search?.takeIf { it.isNotBlank() }?.let { parameter("search", it) }
            }.body()
        }

    suspend fun getFeatureFlag(projectId: Long, flagId: Long) = networkCall<FeatureFlagDto> {
        client.get(authDataStore.projectUrl(projectId, "feature_flags/$flagId/")).body()
    }

    /** Requires a key with the `feature_flag:write` scope; PostHog answers 403 otherwise. */
    suspend fun setActive(projectId: Long, flagId: Long, active: Boolean) = networkCall<FeatureFlagDto> {
        client.patchJson(
            authDataStore.projectUrl(projectId, "feature_flags/$flagId/"),
            buildJsonObject { put("active", active) },
        ).body()
    }
}
