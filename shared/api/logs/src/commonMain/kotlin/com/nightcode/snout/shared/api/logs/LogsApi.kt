package com.nightcode.snout.shared.api.logs

import com.nightcode.snout.shared.api.DateRangeDto
import com.nightcode.snout.shared.api.QueryRequestDto
import com.nightcode.snout.shared.api.logs.model.LogServicesQueryDto
import com.nightcode.snout.shared.api.logs.model.LogServicesResponseDto
import com.nightcode.snout.shared.api.logs.model.LogVolumeBucketDto
import com.nightcode.snout.shared.api.logs.model.LogVolumeQueryDto
import com.nightcode.snout.shared.api.logs.model.LogsQueryDto
import com.nightcode.snout.shared.api.logs.model.LogsQueryResponseDto
import com.nightcode.snout.shared.api.networkCall
import com.nightcode.snout.shared.api.postJson
import com.nightcode.snout.shared.api.projectUrl
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import org.koin.core.annotation.Single

private const val PageSize = 100

/**
 * PostHog's Logs API (OpenTelemetry logs sent to PostHog). Requires the `logs:read` scope; instances without the
 * Logs product answer 404.
 */
@Single
class LogsApi(private val client: HttpClient, private val authDataStore: AuthDataStore) {

    /** Per-service volume and error counts since [dateFrom], a relative date such as `-24h`. */
    suspend fun getServices(projectId: Long, dateFrom: String) = networkCall<LogServicesResponseDto> {
        postQuery(projectId, "logs/services/", LogServicesQueryDto(DateRangeDto(dateFrom))).body()
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
        postQuery(
            projectId,
            "logs/query/",
            LogsQueryDto(
                dateRange = DateRangeDto(dateFrom, dateTo),
                serviceNames = listOf(serviceName),
                severityLevels = severityLevels,
                orderBy = "latest",
                limit = PageSize,
                searchTerm = search?.takeIf { it.isNotBlank() },
                after = after,
            ),
        ).body()
    }

    /** A service's log volume over time, one bucket per level per time slot, filtered like [getLogs]. */
    suspend fun getVolume(
        projectId: Long,
        serviceName: String,
        dateFrom: String,
        severityLevels: List<String>,
        search: String?,
    ) = networkCall<List<LogVolumeBucketDto>> {
        postQuery(
            projectId,
            "logs/sparkline/",
            LogVolumeQueryDto(
                dateRange = DateRangeDto(dateFrom),
                serviceNames = listOf(serviceName),
                severityLevels = severityLevels,
                sparklineBreakdownBy = "severity",
                searchTerm = search?.takeIf { it.isNotBlank() },
            ),
        ).body()
    }

    /** The logs endpoints wrap their parameters in a `query` object. */
    private suspend inline fun <reified Q> postQuery(projectId: Long, path: String, query: Q) =
        client.postJson(authDataStore.projectUrl(projectId, path), QueryRequestDto(query))
}
