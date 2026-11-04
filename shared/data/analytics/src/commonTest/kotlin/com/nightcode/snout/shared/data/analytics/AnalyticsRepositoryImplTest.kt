package com.nightcode.snout.shared.data.analytics

import com.nightcode.snout.shared.api.analytics.AnalyticsApi
import com.nightcode.snout.shared.data.testutils.FakePostHog
import com.nightcode.snout.shared.data.testutils.TestJson
import com.nightcode.snout.shared.data.testutils.jsonBody
import com.nightcode.snout.shared.data.testutils.respondError
import com.nightcode.snout.shared.data.testutils.respondJson
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKey
import com.nightcode.snout.shared.domain.model.analytics.TrendMetric
import com.nightcode.snout.shared.domain.model.analytics.WebBreakdown
import com.nightcode.snout.shared.domain.model.common.DataError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val ProjectId = 1L

private const val Overview = "WebOverviewQuery"
private const val StatsTable = "WebStatsTableQuery"
private const val PropertyBreakdown = "PropertyBreakdown"
private const val MetricTrends = "visitors"

private const val OverviewResponse = """{"results":[{"key":"visitors","value":120.0,"previous":100.0,"kind":"unit"}]}"""
private const val TrendsResponse =
    """{"results":[{"action":{"custom_name":"visitors"},"compare_label":"current","data":[1.0],"days":["2026-10-01"]}]}"""
private const val StatsTableResponse =
    """{"columns":["context.columns.breakdown_value","context.columns.visitors"],"results":[["/home",12]]}"""
private const val PropertyBreakdownResponse = """{"results":[{"breakdown_value":"2.4.3","aggregated_value":120.0}]}"""

/** Which of the tab's requests this is, told apart by the query it posts. */
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

        should("read the overview's stat cards") {
            postHog.onRequest { request ->
                request.analyticsQuery() shouldBe Overview
                respondJson(OverviewResponse)
            }

            repository.getOverview(ProjectId, range).shouldBeRight().map { it.key } shouldBe
                listOf(OverviewMetricKey.Visitors)
        }

        should("pass on PostHog's refusal") {
            postHog.onRequest { respondError(HttpStatusCode.Forbidden) }

            repository.getOverview(ProjectId, range) shouldBeLeft DataError.Forbidden
            repository.getActiveUsers(ProjectId, range) shouldBeLeft DataError.Forbidden
        }

        should("read the trends of visitors, views and sessions") {
            postHog.onRequest { request ->
                request.analyticsQuery() shouldBe MetricTrends
                respondJson(TrendsResponse)
            }

            repository.getTrends(ProjectId, range).shouldBeRight().map { it.metric } shouldBe
                listOf(TrendMetric.Visitors)
        }

        should("fail as unexpected when PostHog answers in a shape it can't read") {
            postHog.onRequest { respondJson("""{"results":"unexpected"}""") }

            repository.getTrends(ProjectId, range) shouldBeLeft DataError.UnexpectedError
        }

        should("read a web analytics table") {
            postHog.onRequest { request ->
                request.analyticsQuery() shouldBe StatsTable
                respondJson(StatsTableResponse)
            }

            val table = repository.getBreakdown(ProjectId, range, WebBreakdown.Page).shouldBeRight()
            table.breakdown shouldBe WebBreakdown.Page
            table.rows.map { it.label } shouldBe listOf("/home")
        }

        should("read an event property breakdown") {
            postHog.onRequest { request ->
                request.analyticsQuery() shouldBe PropertyBreakdown
                respondJson(PropertyBreakdownResponse)
            }

            val table = repository.getBreakdown(ProjectId, range, WebBreakdown.AppVersion).shouldBeRight()
            table.rows.map { it.label } shouldBe listOf("2.4.3")
        }
    })
