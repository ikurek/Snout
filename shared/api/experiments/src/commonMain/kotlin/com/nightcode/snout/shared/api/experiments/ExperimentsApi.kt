package com.nightcode.snout.shared.api.experiments

import com.nightcode.snout.shared.api.PaginatedResponseDto
import com.nightcode.snout.shared.api.experiments.model.ExperimentDto
import com.nightcode.snout.shared.api.networkCall
import com.nightcode.snout.shared.api.projectUrl
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

private const val PageSize = 100

class ExperimentsApi internal constructor(private val client: HttpClient, private val authDataStore: AuthDataStore) {

    suspend fun getExperiments(projectId: Long) = networkCall<PaginatedResponseDto<ExperimentDto>> {
        client.get(authDataStore.projectUrl(projectId, "experiments/")) { parameter("limit", PageSize) }.body()
    }

    suspend fun getExperiment(projectId: Long, experimentId: Long) = networkCall<ExperimentDto> {
        client.get(authDataStore.projectUrl(projectId, "experiments/$experimentId/")).body()
    }
}
