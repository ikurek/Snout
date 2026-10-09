package com.nightcode.snout.shared.api.logs

import com.nightcode.snout.shared.api.logs.model.LogServicesResponseDto
import com.nightcode.snout.shared.api.logs.model.LogVolumeBucketDto
import com.nightcode.snout.shared.api.logs.model.LogsQueryResponseDto
import com.nightcode.snout.shared.api.networkCall
import com.nightcode.snout.shared.api.projectUrl
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

private const val PageSize = 100

/**
 * PostHog's Logs API (OpenTelemetry logs sent to PostHog). Requires the `logs:read` scope; instances without the
 * Logs product answer 404.
 */
class LogsApi internal constructor(private val client: HttpClient, private val authDataStore: AuthDataStore) {

    /** Per-service volume and error counts since [dateFrom], a relative date such as `-24h`. */
    suspend fun getServices(projectId: Long, dateFrom: String) = networkCall<LogServicesResponseDto> {
        post(projectId, "logs/services/") { putDateRange(dateFrom) }.body()
    }

    /**
     * One page of a service's logs, newest first. [severityLevels] empty means every level; pass the previous page's
     * cursor as [after] to continue. [dateTo] defaults to now.
     */
    @Suppress("LongParameterList")
    suspend fun getLogs(
        projectId: Long,
        serviceName: String,
        dateFrom: String,
        severityLevels: List<String>,
        search: String?,
        after: String?,
        dateTo: String? = null,
    ) = networkCall<LogsQueryResponseDto> {
        post(projectId, "logs/query/") {
            putDateRange(dateFrom, dateTo)
            putJsonArray("serviceNames") { add(serviceName) }
            putJsonArray("severityLevels") { severityLevels.forEach { add(it) } }
            put("orderBy", "latest")
            put("limit", PageSize)
            search?.takeIf { it.isNotBlank() }?.let { put("searchTerm", it) }
            after?.let { put("after", it) }
        }.body()
    }

    /** A service's log volume over time, one bucket per level per time slot, filtered like [getLogs]. */
    suspend fun getVolume(
        projectId: Long,
        serviceName: String,
        dateFrom: String,
        severityLevels: List<String>,
        search: String?,
    ) = networkCall<List<LogVolumeBucketDto>> {
        post(projectId, "logs/sparkline/") {
            putDateRange(dateFrom)
            putJsonArray("serviceNames") { add(serviceName) }
            putJsonArray("severityLevels") { severityLevels.forEach { add(it) } }
            put("sparklineBreakdownBy", "severity")
            search?.takeIf { it.isNotBlank() }?.let { put("searchTerm", it) }
        }.body()
    }

    /** The logs endpoints wrap their parameters in a `query` object. */
    private suspend fun post(projectId: Long, path: String, query: JsonObjectBuilder.() -> Unit) =
        client.post(authDataStore.projectUrl(projectId, path)) {
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject { put("query", buildJsonObject(query)) })
        }

    private fun JsonObjectBuilder.putDateRange(dateFrom: String, dateTo: String? = null) = putJsonObject("dateRange") {
        put("date_from", dateFrom)
        dateTo?.let { put("date_to", it) }
    }
}
