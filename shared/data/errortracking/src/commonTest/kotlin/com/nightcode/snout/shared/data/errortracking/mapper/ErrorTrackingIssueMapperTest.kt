package com.nightcode.snout.shared.data.errortracking.mapper

import com.nightcode.snout.shared.api.errortracking.model.ErrorTrackingIssueDto
import com.nightcode.snout.shared.api.errortracking.model.ErrorTrackingIssuesResponseDto
import com.nightcode.snout.shared.api.errortracking.model.HogQLResultsDto
import com.nightcode.snout.shared.domain.model.errortracking.AppVersion
import com.nightcode.snout.shared.domain.model.errortracking.IssueRelease
import com.nightcode.snout.shared.domain.model.errortracking.IssueSeverity
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatus
import com.nightcode.snout.shared.domain.model.errortracking.StackFrame
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlin.time.Instant

class ErrorTrackingIssueMapperTest :
    ShouldSpec({
        val json = Json { ignoreUnknownKeys = true }

        should("map a list row with aggregations and volume") {
            val response = json.decodeFromString<ErrorTrackingIssuesResponseDto>(
                """{"results":[{"id":"0189-a","name":"TypeError","description":"Cannot read 'price'",
                "status":"pending_release","severity":"critical","first_seen":"2026-09-28T08:12:00.123456Z",
                "last_seen":"2026-10-09T07:41:00Z","library":"posthog-js","source":"cart.tsx",
                "assignee":{"id":3,"type":"user"},
                "aggregations":{"occurrences":1284.0,"users":312.0,"sessions":407.0,"volumeRange":[1.0,0.0,3.0],
                "volume_buckets":[{"label":"2026-10-07","value":1.0}]}}],
                "hasMore":false,"limit":100,"offset":0}""",
            )
            val issue = response.results.single().toIssue()
            issue.name shouldBe "TypeError"
            issue.description shouldBe "Cannot read 'price'"
            issue.status shouldBe IssueStatus.PendingRelease
            issue.severity shouldBe IssueSeverity.Critical
            issue.firstSeen shouldBe Instant.parse("2026-09-28T08:12:00.123456Z")
            issue.occurrences shouldBe 1284
            issue.users shouldBe 312
            issue.sessions shouldBe 407
            issue.volume shouldBe listOf(1L, 0L, 3L)
        }

        should("map the detail response's impact, sparkline, frame and release") {
            val issue = json.decodeFromString<ErrorTrackingIssueDto>(
                """{"id":"0189-b","name":"NetworkError","status":"active","severity":null,
                "impact":{"occurrences":18.0,"users":9.0},"sparkline":[6.0,5.0],
                "top_in_app_frame":{"function":"load","source":"api.kt","line":42,"in_app":true},
                "latest_release":{"version":"2.14.0","commit_id":"9f3c2ab"}}""",
            ).toIssue()
            issue.occurrences shouldBe 18
            issue.sessions shouldBe 0
            issue.volume shouldBe listOf(6L, 5L)
            issue.severity.shouldBeNull()
            issue.topFrame shouldBe StackFrame(function = "load", source = "api.kt", line = 42, column = null)
            issue.latestRelease shouldBe IssueRelease(version = "2.14.0", commitId = "9f3c2ab", branch = null)
        }

        should("drop empty frame and release objects and fall back for unnamed issues") {
            val issue = json.decodeFromString<ErrorTrackingIssueDto>(
                """{"id":"0189-c","name":"","description":"Script error.","status":"something_new",
                "impact":{},"top_in_app_frame":{},"latest_release":{}}""",
            ).toIssue()
            issue.name shouldBe "Script error."
            issue.description.shouldBeNull()
            issue.status shouldBe IssueStatus.Unknown
            issue.occurrences shouldBe 0
            issue.volume shouldBe emptyList()
            issue.topFrame.shouldBeNull()
            issue.latestRelease.shouldBeNull()
        }

        should("read app versions from HogQL rows and skip malformed ones") {
            val versions = json.decodeFromString<HogQLResultsDto>(
                """{"results":[["2.14.0",1102],["2.13.2",186],[null,4],[""," 3"],["1.0"]],
                "columns":["version","exceptions"]}""",
            ).toAppVersions()
            versions shouldBe listOf(AppVersion("2.14.0", 1102), AppVersion("2.13.2", 186))
        }
    })
