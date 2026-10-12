package com.nightcode.snout.shared.api.analytics

import com.nightcode.snout.shared.data.testutils.FakePostHog
import com.nightcode.snout.shared.data.testutils.TestInstanceUrl
import com.nightcode.snout.shared.data.testutils.jsonBody
import com.nightcode.snout.shared.data.testutils.parseJson
import com.nightcode.snout.shared.data.testutils.respondJson
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod

private const val ProjectId = 1L

/** The two series PostHog gets per trends series: one per visit event, both reported under [name]. */
private fun visitNodes(name: String, math: String) = listOf("\$pageview", "\$screen").joinToString(",") { event ->
    """{"kind":"EventsNode","event":"$event","name":"$event","custom_name":"$name","math":"$math"}"""
}

/** Pins the query bodies sent to PostHog, so refactoring how they are built cannot change the wire format. */
class AnalyticsApiTest :
    ShouldSpec({
        val postHog = FakePostHog()
        val api = AnalyticsApi(postHog.client, postHog.authDataStore)
        postHog.onRequest { respondJson("""{"results":[]}""") }

        suspend fun shouldHaveQueried(body: String) {
            val request = postHog.requests.single()
            request.method shouldBe HttpMethod.Post
            request.url.toString() shouldBe "$TestInstanceUrl/api/projects/$ProjectId/query/"
            request.jsonBody() shouldBe parseJson(body)
        }

        should("send the web overview query") {
            api.getWebOverview(ProjectId, dateFrom = "-7d")

            shouldHaveQueried(
                """
                {"query":{
                    "kind":"WebOverviewQuery",
                    "dateRange":{"date_from":"-7d"},
                    "properties":[],
                    "compareFilter":{"compare":true}
                }}
                """,
            )
        }

        should("send the web stats table query") {
            api.getWebStatsTable(ProjectId, dateFrom = "-30d", breakdownBy = "Page", limit = 10)

            shouldHaveQueried(
                """
                {"query":{
                    "kind":"WebStatsTableQuery",
                    "breakdownBy":"Page",
                    "dateRange":{"date_from":"-30d"},
                    "properties":[],
                    "limit":10,
                    "compareFilter":{"compare":false}
                }}
                """,
            )
        }

        should("send the metric trends query with the preceding period") {
            api.getMetricTrends(ProjectId, dateFrom = "-7d", interval = "hour")

            val series = listOf(
                visitNodes("visitors", "dau"),
                visitNodes("views", "total"),
                visitNodes("sessions", "unique_session"),
            ).joinToString(",")
            shouldHaveQueried(
                """
                {"query":{
                    "kind":"TrendsQuery",
                    "dateRange":{"date_from":"-7d"},
                    "interval":"hour",
                    "series":[$series],
                    "trendsFilter":{"display":"ActionsLineGraph"},
                    "compareFilter":{"compare":true}
                }}
                """,
            )
        }

        should("send the active users query per day with the preceding period") {
            api.getActiveUsers(ProjectId, dateFrom = "-90d")

            shouldHaveQueried(
                """
                {"query":{
                    "kind":"TrendsQuery",
                    "dateRange":{"date_from":"-90d"},
                    "interval":"day",
                    "series":[${visitNodes("wau", "weekly_active")},${visitNodes("mau", "monthly_active")}],
                    "trendsFilter":{"display":"ActionsLineGraph"},
                    "compareFilter":{"compare":true}
                }}
                """,
            )
        }

        should("send the property breakdown query as a totals table without comparison") {
            api.getPropertyBreakdown(ProjectId, dateFrom = "-7d", property = "\$browser", limit = 5)

            shouldHaveQueried(
                """
                {"query":{
                    "kind":"TrendsQuery",
                    "dateRange":{"date_from":"-7d"},
                    "interval":"day",
                    "series":[${visitNodes("visitors", "dau")}],
                    "trendsFilter":{"display":"ActionsBarValue"},
                    "breakdownFilter":{"breakdown":"${'$'}browser","breakdown_type":"event","breakdown_limit":5}
                }}
                """,
            )
        }
    })
