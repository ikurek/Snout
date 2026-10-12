package com.nightcode.snout.shared.api.logs

import com.nightcode.snout.shared.data.testutils.FakePostHog
import com.nightcode.snout.shared.data.testutils.TestInstanceUrl
import com.nightcode.snout.shared.data.testutils.jsonBody
import com.nightcode.snout.shared.data.testutils.parseJson
import com.nightcode.snout.shared.data.testutils.respondJson
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod

private const val ProjectId = 1L

/** Pins the query bodies sent to PostHog, so refactoring how they are built cannot change the wire format. */
class LogsApiTest :
    ShouldSpec({
        val postHog = FakePostHog()
        val api = LogsApi(postHog.client, postHog.authDataStore)
        postHog.onRequest { request ->
            respondJson(if (request.url.encodedPath.endsWith("sparkline/")) "[]" else """{"results":[]}""")
        }

        suspend fun shouldHavePosted(path: String, body: String) {
            val request = postHog.requests.single()
            request.method shouldBe HttpMethod.Post
            request.url.toString() shouldBe "$TestInstanceUrl/api/projects/$ProjectId/$path"
            request.jsonBody() shouldBe parseJson(body)
        }

        should("query services since a date") {
            api.getServices(ProjectId, dateFrom = "-24h")

            shouldHavePosted("logs/services/", """{"query":{"dateRange":{"date_from":"-24h"}}}""")
        }

        should("query logs with every optional filter left out") {
            api.getLogs(
                ProjectId,
                serviceName = "api",
                dateFrom = "-1h",
                severityLevels = emptyList(),
                search = null,
                after = null,
            )

            shouldHavePosted(
                "logs/query/",
                """
                {"query":{
                    "dateRange":{"date_from":"-1h"},
                    "serviceNames":["api"],
                    "severityLevels":[],
                    "orderBy":"latest",
                    "limit":100
                }}
                """,
            )
        }

        should("leave a blank search out of the logs query") {
            api.getLogs(
                ProjectId,
                serviceName = "api",
                dateFrom = "-1h",
                severityLevels = listOf("error"),
                search = " ",
                after = null,
            )

            shouldHavePosted(
                "logs/query/",
                """
                {"query":{
                    "dateRange":{"date_from":"-1h"},
                    "serviceNames":["api"],
                    "severityLevels":["error"],
                    "orderBy":"latest",
                    "limit":100
                }}
                """,
            )
        }

        should("query logs with every optional filter set") {
            api.getLogs(
                ProjectId,
                serviceName = "api",
                dateFrom = "2026-10-01T09:59:59Z",
                severityLevels = listOf("warn", "error"),
                search = "timeout",
                after = "cursor-1",
                dateTo = "2026-10-01T10:00:01Z",
            )

            shouldHavePosted(
                "logs/query/",
                """
                {"query":{
                    "dateRange":{"date_from":"2026-10-01T09:59:59Z","date_to":"2026-10-01T10:00:01Z"},
                    "serviceNames":["api"],
                    "severityLevels":["warn","error"],
                    "orderBy":"latest",
                    "limit":100,
                    "searchTerm":"timeout",
                    "after":"cursor-1"
                }}
                """,
            )
        }

        should("query volume without a search") {
            api.getVolume(ProjectId, serviceName = "api", dateFrom = "-1h", severityLevels = emptyList(), search = null)

            shouldHavePosted(
                "logs/sparkline/",
                """
                {"query":{
                    "dateRange":{"date_from":"-1h"},
                    "serviceNames":["api"],
                    "severityLevels":[],
                    "sparklineBreakdownBy":"severity"
                }}
                """,
            )
        }

        should("query volume with a search and levels") {
            api.getVolume(
                ProjectId,
                serviceName = "api",
                dateFrom = "-7d",
                severityLevels = listOf("info"),
                search = "timeout",
            )

            shouldHavePosted(
                "logs/sparkline/",
                """
                {"query":{
                    "dateRange":{"date_from":"-7d"},
                    "serviceNames":["api"],
                    "severityLevels":["info"],
                    "sparklineBreakdownBy":"severity",
                    "searchTerm":"timeout"
                }}
                """,
            )
        }
    })
