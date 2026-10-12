package com.nightcode.snout.shared.api.insights

import com.nightcode.snout.shared.api.PaginatedResponseDto
import com.nightcode.snout.shared.api.insights.model.InsightDto
import com.nightcode.snout.shared.api.networkCall
import com.nightcode.snout.shared.api.patchJson
import com.nightcode.snout.shared.api.projectUrl
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val PageSize = 100

/** PostHog answers `refresh=blocking` only once the query has run; breakdown funnels take well past 10 s. */
private const val BlockingRefreshTimeoutMillis = 90_000L

class InsightsApi internal constructor(private val client: HttpClient, private val authDataStore: AuthDataStore) {

    // Limitation: first 100 saved insights only; add offset paging when a project outgrows that.
    suspend fun getInsights(projectId: Long, search: String? = null) = networkCall<PaginatedResponseDto<InsightDto>> {
        client.get(authDataStore.projectUrl(projectId, "insights/")) {
            parameter("saved", true)
            parameter("basic", true)
            parameter("order", "-last_modified_at")
            parameter("limit", PageSize)
            search?.takeIf { it.isNotBlank() }?.let { parameter("search", it) }
        }.body()
    }

    /** Insight links carry the short id; the API filters on it and `basic` skips computing the result. */
    suspend fun findInsightsByShortId(projectId: Long, shortId: String) =
        networkCall<PaginatedResponseDto<InsightDto>> {
            client.get(authDataStore.projectUrl(projectId, "insights/")) {
                parameter("short_id", shortId)
                parameter("basic", true)
            }.body()
        }

    suspend fun getInsight(projectId: Long, insightId: Long, fromDashboardId: Long? = null) = networkCall<InsightDto> {
        client.get(authDataStore.projectUrl(projectId, "insights/$insightId/")) {
            parameter("refresh", "blocking")
            timeout {
                requestTimeoutMillis = BlockingRefreshTimeoutMillis
                socketTimeoutMillis = BlockingRefreshTimeoutMillis
            }
            fromDashboardId?.let { parameter("from_dashboard", it) }
        }.body()
    }

    suspend fun setFavorite(projectId: Long, insightId: Long, favorited: Boolean) = networkCall<InsightDto> {
        client.patchJson(
            authDataStore.projectUrl(projectId, "insights/$insightId/"),
            buildJsonObject { put("favorited", favorited) },
        ).body()
    }
}
