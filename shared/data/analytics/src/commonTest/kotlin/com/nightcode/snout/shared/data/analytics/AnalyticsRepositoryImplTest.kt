package com.nightcode.snout.shared.data.analytics

import com.nightcode.snout.shared.api.analytics.AnalyticsApi
import com.nightcode.snout.shared.data.testutils.FakePostHog
import com.nightcode.snout.shared.data.testutils.TestJson
import com.nightcode.snout.shared.data.testutils.jsonBody
import com.nightcode.snout.shared.data.testutils.respondError
import com.nightcode.snout.shared.data.testutils.respondJson
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPreferences
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKey
import com.nightcode.snout.shared.domain.model.analytics.TrendMetric
import com.nightcode.snout.shared.domain.model.analytics.WebBreakdown
import com.nightcode.snout.shared.domain.model.common.DataError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val ProjectId = 1L

private const val Overview = "WebOverviewQuery"
private const val StatsTable = "WebStatsTableQuery"
private const val PropertyBreakdown = "PropertyBreakdown"
private const val MetricTrends = "visitors"
private const val ActiveUsers = "wau"

private const val OverviewResponse = """{"results":[{"key":"visitors","value":120.0,"previous":100.0,"kind":"unit"}]}"""
private const val TrendsResponse =
    """{"results":[{"action":{"custom_name":"visitors"},"compare_label":"current","data":[1.0],"days":["2026-10-01"]}]}"""
private const val StatsTableResponse =
    """{"columns":["context.columns.breakdown_value","context.columns.visitors"],"results":[["/home",12]]}"""
private const val PropertyBreakdownResponse = """{"results":[{"breakdown_value":"2.4.3","aggregated_value":120.0}]}"""

/** Which of the snapshot's requests this is, told apart by the query it posts. */
private suspend fun HttpRequestData.analyticsQuery(): String {
    val query = jsonBody().getValue("query").jsonObject
    val kind = query.getValue("kind").jsonPrimitive.content
    return when {
        "breakdownFilter" in query -> PropertyBreakdown

        kind == "TrendsQuery" ->
            query.getValue("series").jsonArray.first().jsonObject.getValue("custom_name").jsonPrimitive.content

        else -> kind
    }
}

class AnalyticsRepositoryImplTest :
    ShouldSpec({
        val postHog = FakePostHog()
        val repository = AnalyticsRepositoryImpl(
            AnalyticsApi(postHog.client, postHog.authDataStore),
            TestJson,
        )
        val range = AnalyticsRange.Last7Days

        should("keep the snapshot when optional requests fail") {
            postHog.onRequest { request ->
                when (request.analyticsQuery()) {
                    Overview -> respondJson(OverviewResponse)
                    MetricTrends -> respondJson(TrendsResponse)
                    StatsTable -> respondJson(StatsTableResponse)
                    ActiveUsers -> respondError(HttpStatusCode.Forbidden)
                    else -> respondError(HttpStatusCode.InternalServerError)
                }
            }

            val snapshot = repository.getSnapshot(ProjectId, range, AnalyticsPreferences.Default).shouldBeRight()
            snapshot.overview.map { it.key } shouldBe listOf(OverviewMetricKey.Visitors)
            snapshot.trends.map { it.metric } shouldBe listOf(TrendMetric.Visitors)
            snapshot.breakdowns.map { it.breakdown } shouldBe WebBreakdown.entries - WebBreakdown.AppVersion
        }

        should("leave out an optional part PostHog answers in an unreadable shape") {
            postHog.onRequest { request ->
                when (request.analyticsQuery()) {
                    Overview -> respondJson(OverviewResponse)
                    MetricTrends -> respondJson("""{"results":"unexpected"}""")
                    else -> respondError(HttpStatusCode.InternalServerError)
                }
            }

            val snapshot = repository.getSnapshot(ProjectId, range, AnalyticsPreferences.Default).shouldBeRight()
            snapshot.overview.map { it.key } shouldBe listOf(OverviewMetricKey.Visitors)
            snapshot.trends shouldBe emptyList()
        }

        should("fail when the overview fails") {
            postHog.onRequest { request ->
                when (request.analyticsQuery()) {
                    Overview -> respondError(HttpStatusCode.Forbidden)
                    StatsTable -> respondJson(StatsTableResponse)
                    PropertyBreakdown -> respondJson(PropertyBreakdownResponse)
                    else -> respondJson(TrendsResponse)
                }
            }

            repository.getSnapshot(ProjectId, range, AnalyticsPreferences.Default) shouldBeLeft DataError.Forbidden
        }

        should("request only what the preferences ask for") {
            val requested = MutableStateFlow(emptyList<String>())
            postHog.onRequest { request ->
                val query = request.analyticsQuery()
                requested.update { it + query }
                respondJson(if (query == Overview) OverviewResponse else StatsTableResponse)
            }
            val preferences = AnalyticsPreferences.Default.copy(
                metrics = setOf(OverviewMetricKey.Visitors),
                showTrendChart = false,
                breakdowns = setOf(WebBreakdown.Page),
            )

            repository.getSnapshot(ProjectId, range, preferences).shouldBeRight()

            requested.value shouldContainExactlyInAnyOrder listOf(Overview, StatsTable)
        }
    })
