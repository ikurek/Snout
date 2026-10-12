package com.nightcode.snout.shared.domain.model.errortracking

import com.nightcode.snout.shared.common.testdata.TestData
import kotlin.time.Instant

enum class IssueStatus {
    Active,
    Resolved,
    Suppressed,
    Archived,

    /** Resolved, waiting for a release that contains the fix. */
    PendingRelease,
    Unknown,
}

/** The statuses an issue can be set to, which are also the ones the issue list can be narrowed to. */
enum class IssueStatusFilter {
    Active,
    Resolved,
    Suppressed,
}

enum class IssueSeverity {
    Low,
    Medium,
    High,
    Critical,
}

/** Where the exception was thrown: the topmost frame of the app's own code. */
data class StackFrame(val function: String?, val source: String?, val line: Int?, val column: Int?)

data class IssueRelease(val version: String?, val commitId: String?, val branch: String?)

/** A group of exceptions PostHog considers the same bug, with its impact over the last seven days. */
data class ErrorTrackingIssue(
    val id: String,
    /** Exception type, such as `TypeError`. */
    val name: String,
    /** Exception message; a short preview in lists, complete on the detail screen. */
    val description: String?,
    val status: IssueStatus,
    val severity: IssueSeverity?,
    val firstSeen: Instant?,
    val lastSeen: Instant?,
    /** SDK that reported it, such as `posthog-js`. */
    val library: String?,
    val source: String?,
    val occurrences: Long,
    val users: Long,
    val sessions: Long,
    /** Occurrences per day over the last seven days, oldest first; empty when PostHog sent no buckets. */
    val volume: List<Long>,
    val topFrame: StackFrame? = null,
    val latestRelease: IssueRelease? = null,
)

/** A version of the app, from the `$app_version` PostHog SDKs attach, that reported exceptions this week. */
data class AppVersion(val name: String, val exceptions: Long)

@TestData
val TestErrorTrackingIssue = ErrorTrackingIssue(
    id = "01890a4f-6f3c-7c2e-9a1b-3d5e7f9a0b1c",
    name = "TypeError",
    description = "Cannot read properties of undefined (reading 'price')",
    status = IssueStatus.Active,
    severity = IssueSeverity.High,
    firstSeen = Instant.parse("2026-09-28T08:12:00Z"),
    lastSeen = Instant.parse("2026-10-06T09:41:00Z"),
    library = "posthog-js",
    source = "src/checkout/CartSummary.tsx",
    occurrences = 1_284,
    users = 312,
    sessions = 407,
    volume = listOf(96, 120, 154, 210, 188, 236, 280),
    topFrame = StackFrame(function = "CartSummary", source = "src/checkout/CartSummary.tsx", line = 42, column = 17),
    latestRelease = IssueRelease(version = "2.14.0", commitId = "9f3c2ab", branch = "main"),
)

@TestData
@OptIn(TestData::class)
val TestResolvedErrorTrackingIssue = TestErrorTrackingIssue.copy(
    id = "01890a4f-6f3c-7c2e-9a1b-3d5e7f9a0b2d",
    name = "NetworkError",
    description = "Failed to fetch",
    status = IssueStatus.Resolved,
    severity = null,
    library = "posthog-android",
    source = null,
    occurrences = 18,
    users = 9,
    sessions = 11,
    volume = listOf(6, 5, 4, 2, 1, 0, 0),
    topFrame = null,
    latestRelease = null,
)

@TestData
val TestAppVersions = listOf(AppVersion("2.14.0", exceptions = 1_102), AppVersion("2.13.2", exceptions = 186))
