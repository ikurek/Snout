package com.nightcode.snout.shared.api.analytics

import com.nightcode.snout.shared.api.QueryRequestDto
import com.nightcode.snout.shared.api.analytics.model.QueryResponseDto
import com.nightcode.snout.shared.api.networkCall
import com.nightcode.snout.shared.api.postJson
import com.nightcode.snout.shared.api.projectUrl
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import org.koin.core.annotation.Single

/** Series names used in trends requests and matched again in results. */
object TrendSeriesNames {
    const val Visitors = "visitors"
    const val Views = "views"
    const val Sessions = "sessions"
    const val WeeklyActiveUsers = "wau"
    const val MonthlyActiveUsers = "mau"
}

private val MetricTrendSeries = listOf(
    TrendSeriesSpec(TrendSeriesNames.Visitors, "dau"),
    TrendSeriesSpec(TrendSeriesNames.Views, "total"),
    TrendSeriesSpec(TrendSeriesNames.Sessions, "unique_session"),
)

private val ActiveUserSeries = listOf(
    TrendSeriesSpec(TrendSeriesNames.WeeklyActiveUsers, "weekly_active"),
    TrendSeriesSpec(TrendSeriesNames.MonthlyActiveUsers, "monthly_active"),
)

private val BreakdownSeries = listOf(TrendSeriesSpec(TrendSeriesNames.Visitors, "dau"))

@Single
class AnalyticsApi(private val client: HttpClient, private val authDataStore: AuthDataStore) {

    suspend fun getWebOverview(projectId: Long, dateFrom: String) = runQuery(projectId, webOverviewQuery(dateFrom))

    suspend fun getWebStatsTable(projectId: Long, dateFrom: String, breakdownBy: String, limit: Int) =
        runQuery(projectId, webStatsTableQuery(dateFrom, breakdownBy, limit))

    /** Visitors, views and sessions over time, each with the preceding period for comparison. */
    suspend fun getMetricTrends(projectId: Long, dateFrom: String, interval: String) =
        runQuery(projectId, trendsQuery(dateFrom, interval, MetricTrendSeries, compare = true))

    /** Rolling weekly and monthly active users per day; the last point is the value "as of" the range end. */
    suspend fun getActiveUsers(projectId: Long, dateFrom: String) =
        runQuery(projectId, trendsQuery(dateFrom, interval = "day", series = ActiveUserSeries, compare = true))

    /** Unique visitors per value of an event property over the whole range. */
    suspend fun getPropertyBreakdown(projectId: Long, dateFrom: String, property: String, limit: Int) = runQuery(
        projectId,
        trendsQuery(
            dateFrom,
            interval = "day",
            series = BreakdownSeries,
            breakdownProperty = property,
            breakdownLimit = limit,
        ),
    )

    /** Reified so the query serializes as its own DTO, with its `kind`. */
    private suspend inline fun <reified Q> runQuery(projectId: Long, query: Q) = networkCall<QueryResponseDto> {
        client.postJson(authDataStore.projectUrl(projectId, "query/"), QueryRequestDto(query)).body()
    }
}
