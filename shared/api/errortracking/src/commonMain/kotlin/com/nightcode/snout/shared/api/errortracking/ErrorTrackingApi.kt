package com.nightcode.snout.shared.api.errortracking

import com.nightcode.snout.shared.api.errortracking.model.ErrorTrackingIssueDto
import com.nightcode.snout.shared.api.errortracking.model.ErrorTrackingIssuesResponseDto
import com.nightcode.snout.shared.api.errortracking.model.HogQLResultsDto
import com.nightcode.snout.shared.api.networkCall
import com.nightcode.snout.shared.api.projectUrl
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/** PostHog caps a page of issues at 100. */
private const val PageSize = 100

/** Issues are aggregated over the last week, one volume bucket per day. */
private const val DateFrom = "-7d"
private const val VolumeBuckets = 7

/** Most recently seen app versions first, so the newest release tops the picker. */
private const val AppVersionsQuery = """
SELECT properties.${'$'}app_version AS version, count() AS exceptions
FROM events
WHERE event = '${'$'}exception' AND timestamp > now() - INTERVAL 7 DAY AND version != ''
GROUP BY version
ORDER BY max(timestamp) DESC
LIMIT 20
"""

/**
 * The compact error tracking endpoints, available on PostHog since mid-2026; older self-hosted instances answer 404.
 * Requires the `error_tracking:read` scope.
 */
class ErrorTrackingApi internal constructor(private val client: HttpClient, private val authDataStore: AuthDataStore) {

    /**
     * [status] is one of active, resolved, suppressed, archived, pending_release or all. [appVersion] keeps only
     * exceptions whose `${'$'}app_version` matches, so counts cover that version alone.
     *
     * Limitation: first 100 issues only; add offset paging when a project outgrows that.
     */
    suspend fun getIssues(projectId: Long, status: String, search: String? = null, appVersion: String? = null) =
        networkCall<ErrorTrackingIssuesResponseDto> {
            post(
                projectId,
                "error_tracking/query/issues/",
                buildJsonObject {
                    put("status", status)
                    put("orderBy", "occurrences")
                    put("limit", PageSize)
                    put("volumeResolution", VolumeBuckets)
                    putDateRange()
                    search?.takeIf { it.isNotBlank() }?.let { put("searchQuery", it) }
                    appVersion?.let { version ->
                        putJsonArray("filterGroup") {
                            add(
                                buildJsonObject {
                                    put("type", "event")
                                    put("key", "${'$'}app_version")
                                    put("operator", "exact")
                                    putJsonArray("value") { add(version) }
                                },
                            )
                        }
                    }
                },
            ).body()
        }

    suspend fun getIssue(projectId: Long, issueId: String) = networkCall<ErrorTrackingIssueDto> {
        post(
            projectId,
            "error_tracking/query/issue/",
            buildJsonObject {
                put("issueId", issueId)
                put("includeSparkline", true)
                put("volumeResolution", VolumeBuckets)
                putDateRange()
            },
        ).body()
    }

    /**
     * Sets an issue's status to active, resolved or suppressed. Requires the `error_tracking:write` scope; PostHog
     * answers 403 otherwise.
     */
    suspend fun setStatus(projectId: Long, issueId: String, status: String) = networkCall<ErrorTrackingIssueDto> {
        client.patch(authDataStore.projectUrl(projectId, "error_tracking/issues/$issueId/")) {
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject { put("status", status) })
        }.body()
    }

    /**
     * App versions that reported exceptions in the last week, with their exception counts. Runs HogQL through the
     * query endpoint, so it needs `query:read` rather than an error tracking scope.
     */
    suspend fun getAppVersions(projectId: Long) = networkCall<HogQLResultsDto> {
        post(
            projectId,
            "query/",
            buildJsonObject {
                putJsonObject("query") {
                    put("kind", "HogQLQuery")
                    put("query", AppVersionsQuery.trimIndent())
                }
            },
        ).body()
    }

    private suspend fun post(projectId: Long, path: String, body: JsonObject) =
        client.post(authDataStore.projectUrl(projectId, path)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

    private fun JsonObjectBuilder.putDateRange() = putJsonObject("dateRange") { put("date_from", DateFrom) }
}
