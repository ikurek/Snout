package com.nightcode.snout.shared.api.insights

import com.nightcode.snout.shared.api.PaginatedResponseDto
import com.nightcode.snout.shared.api.insights.model.DashboardDetailDto
import com.nightcode.snout.shared.api.insights.model.DashboardDto
import com.nightcode.snout.shared.api.networkCall
import com.nightcode.snout.shared.api.projectUrl
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.koin.core.annotation.Single

private const val PageSize = 100

@Single
class DashboardsApi(private val client: HttpClient, private val authDataStore: AuthDataStore) {

    suspend fun getDashboards(projectId: Long) = networkCall<PaginatedResponseDto<DashboardDto>> {
        client.get(authDataStore.projectUrl(projectId, "dashboards/")) { parameter("limit", PageSize) }.body()
    }

    /** Returns tiles with whatever results PostHog has cached; missing ones are computed per insight. */
    suspend fun getDashboard(projectId: Long, dashboardId: Long) = networkCall<DashboardDetailDto> {
        client.get(authDataStore.projectUrl(projectId, "dashboards/$dashboardId/")) {
            parameter("refresh", "force_cache")
        }.body()
    }
}
