package com.nightcode.snout.shared.api.analytics

import arrow.core.Either
import com.nightcode.snout.shared.api.NetworkError
import com.nightcode.snout.shared.api.analytics.model.QueryResponseDto
import com.nightcode.snout.shared.api.networkCall
import com.nightcode.snout.shared.api.projectUrl
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

class AnalyticsApi internal constructor(private val client: HttpClient, private val authDataStore: AuthDataStore) {

    suspend fun getWebOverview(projectId: Long, dateFrom: String) = runQuery(projectId, webOverviewQuery(dateFrom))

    suspend fun getWebStatsTable(projectId: Long, dateFrom: String, breakdownBy: String, limit: Int) =
        runQuery(projectId, webStatsTableQuery(dateFrom, breakdownBy, limit))

    suspend fun getPageviewTrend(projectId: Long, dateFrom: String, interval: String) =
        runQuery(projectId, pageviewTrendQuery(dateFrom, interval))

    private suspend fun runQuery(projectId: Long, query: JsonObject): Either<NetworkError, QueryResponseDto> =
        networkCall {
            client.post(authDataStore.projectUrl(projectId, "query/")) {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("query", query) })
            }.body()
        }
}
