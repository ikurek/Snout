package com.nightcode.snout.shared.api.errortracking

import com.nightcode.snout.shared.data.testutils.FakePostHog
import com.nightcode.snout.shared.data.testutils.TestInstanceUrl
import com.nightcode.snout.shared.data.testutils.jsonBody
import com.nightcode.snout.shared.data.testutils.parseJson
import com.nightcode.snout.shared.data.testutils.respondJson
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod

private const val ProjectId = 1L

/** Pins the request bodies sent to PostHog, so refactoring how they are built cannot change the wire format. */
class ErrorTrackingApiTest :
    ShouldSpec({
        val postHog = FakePostHog()
        val api = ErrorTrackingApi(postHog.client, postHog.authDataStore)
        postHog.onRequest { respondJson("""{"id":"issue-1","results":[]}""") }

        suspend fun shouldHaveSent(method: HttpMethod, path: String, body: String) {
            val request = postHog.requests.single()
            request.method shouldBe method
            request.url.toString() shouldBe "$TestInstanceUrl/api/projects/$ProjectId/$path"
            request.jsonBody() shouldBe parseJson(body)
        }

        should("query issues with only a status") {
            api.getIssues(ProjectId, status = "active")

            shouldHaveSent(
                HttpMethod.Post,
                "error_tracking/query/issues/",
                """
                {
                    "status":"active",
                    "orderBy":"occurrences",
                    "limit":100,
                    "volumeResolution":7,
                    "dateRange":{"date_from":"-7d"}
                }
                """,
            )
        }

        should("leave a blank search out of the issues query") {
            api.getIssues(ProjectId, status = "all", search = "  ")

            shouldHaveSent(
                HttpMethod.Post,
                "error_tracking/query/issues/",
                """
                {
                    "status":"all",
                    "orderBy":"occurrences",
                    "limit":100,
                    "volumeResolution":7,
                    "dateRange":{"date_from":"-7d"}
                }
                """,
            )
        }

        should("query issues with a search and an app version filter") {
            api.getIssues(ProjectId, status = "resolved", search = "NullPointer", appVersion = "2.1.0")

            shouldHaveSent(
                HttpMethod.Post,
                "error_tracking/query/issues/",
                """
                {
                    "status":"resolved",
                    "orderBy":"occurrences",
                    "limit":100,
                    "volumeResolution":7,
                    "dateRange":{"date_from":"-7d"},
                    "searchQuery":"NullPointer",
                    "filterGroup":[
                        {"type":"event","key":"${'$'}app_version","operator":"exact","value":["2.1.0"]}
                    ]
                }
                """,
            )
        }

        should("query one issue with its sparkline") {
            api.getIssue(ProjectId, issueId = "issue-1")

            shouldHaveSent(
                HttpMethod.Post,
                "error_tracking/query/issue/",
                """
                {
                    "issueId":"issue-1",
                    "includeSparkline":true,
                    "volumeResolution":7,
                    "dateRange":{"date_from":"-7d"}
                }
                """,
            )
        }

        should("patch an issue's status") {
            api.setStatus(ProjectId, issueId = "issue-1", status = "suppressed")

            shouldHaveSent(HttpMethod.Patch, "error_tracking/issues/issue-1/", """{"status":"suppressed"}""")
        }

        should("query app versions with HogQL") {
            api.getAppVersions(ProjectId)

            // Joined with JSON's escaped newline: the query is sent as one string with its line breaks.
            val hogQl = listOf(
                "SELECT properties.${'$'}app_version AS version, count() AS exceptions",
                "FROM events",
                "WHERE event = '${'$'}exception' AND timestamp > now() - INTERVAL 7 DAY AND version != ''",
                "GROUP BY version",
                "ORDER BY max(timestamp) DESC",
                "LIMIT 20",
            ).joinToString("\\n")
            shouldHaveSent(
                HttpMethod.Post,
                "query/",
                """{"query":{"kind":"HogQLQuery","query":"$hogQl"}}""",
            )
        }
    })
