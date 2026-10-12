package com.nightcode.snout.shared.api.errortracking

import com.nightcode.snout.shared.api.DateRangeDto
import com.nightcode.snout.shared.api.PropertyFilterDto
import com.nightcode.snout.shared.api.QueryRequestDto
import com.nightcode.snout.shared.api.errortracking.model.ErrorTrackingIssueDto
import com.nightcode.snout.shared.api.errortracking.model.ErrorTrackingIssueRequestDto
import com.nightcode.snout.shared.api.errortracking.model.ErrorTrackingIssuesRequestDto
import com.nightcode.snout.shared.api.errortracking.model.ErrorTrackingIssuesResponseDto
import com.nightcode.snout.shared.api.errortracking.model.HogQLQueryDto
import com.nightcode.snout.shared.api.errortracking.model.HogQLResultsDto
import com.nightcode.snout.shared.api.errortracking.model.UpdateErrorTrackingIssueRequestDto
import com.nightcode.snout.shared.api.networkCall
import com.nightcode.snout.shared.api.patchJson
import com.nightcode.snout.shared.api.postJson
import com.nightcode.snout.shared.api.projectUrl
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import org.koin.core.annotation.Single

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
@Single
class ErrorTrackingApi(private val client: HttpClient, private val authDataStore: AuthDataStore) {

    /**
     * [status] is one of active, resolved, suppressed, archived, pending_release or all. [appVersion] keeps only
     * exceptions whose `${'$'}app_version` matches, so counts cover that version alone.
     *
     * Limitation: first 100 issues only; add offset paging when a project outgrows that.
     */
    suspend fun getIssues(projectId: Long, status: String, search: String? = null, appVersion: String? = null) =
        networkCall<ErrorTrackingIssuesResponseDto> {
            client.postJson(
                authDataStore.projectUrl(projectId, "error_tracking/query/issues/"),
                ErrorTrackingIssuesRequestDto(
                    status = status,
                    orderBy = "occurrences",
                    limit = PageSize,
                    volumeResolution = VolumeBuckets,
                    dateRange = DateRangeDto(DateFrom),
                    searchQuery = search?.takeIf { it.isNotBlank() },
                    filterGroup = appVersion?.let { version ->
                        listOf(
                            PropertyFilterDto(
                                type = "event",
                                key = "${'$'}app_version",
                                operator = "exact",
                                value = listOf(version),
                            ),
                        )
                    },
                ),
            ).body()
        }

    suspend fun getIssue(projectId: Long, issueId: String) = networkCall<ErrorTrackingIssueDto> {
        client.postJson(
            authDataStore.projectUrl(projectId, "error_tracking/query/issue/"),
            ErrorTrackingIssueRequestDto(
                issueId = issueId,
                includeSparkline = true,
                volumeResolution = VolumeBuckets,
                dateRange = DateRangeDto(DateFrom),
            ),
        ).body()
    }

    /**
     * Sets an issue's status to active, resolved or suppressed. Requires the `error_tracking:write` scope; PostHog
     * answers 403 otherwise.
     */
    suspend fun setStatus(projectId: Long, issueId: String, status: String) = networkCall<ErrorTrackingIssueDto> {
        client.patchJson(
            authDataStore.projectUrl(projectId, "error_tracking/issues/$issueId/"),
            UpdateErrorTrackingIssueRequestDto(status = status),
        ).body()
    }

    /**
     * App versions that reported exceptions in the last week, with their exception counts. Runs HogQL through the
     * query endpoint, so it needs `query:read` rather than an error tracking scope.
     */
    suspend fun getAppVersions(projectId: Long) = networkCall<HogQLResultsDto> {
        client.postJson(
            authDataStore.projectUrl(projectId, "query/"),
            QueryRequestDto(HogQLQueryDto(query = AppVersionsQuery.trimIndent())),
        ).body()
    }
}
