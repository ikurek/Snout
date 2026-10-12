package com.nightcode.snout.shared.data.errortracking.mapper

import com.nightcode.snout.shared.api.errortracking.model.ErrorTrackingFrameDto
import com.nightcode.snout.shared.api.errortracking.model.ErrorTrackingIssueDto
import com.nightcode.snout.shared.api.errortracking.model.ErrorTrackingReleaseDto
import com.nightcode.snout.shared.api.errortracking.model.HogQLResultsDto
import com.nightcode.snout.shared.data.common.nullIfBlank
import com.nightcode.snout.shared.data.common.string
import com.nightcode.snout.shared.domain.model.errortracking.AppVersion
import com.nightcode.snout.shared.domain.model.errortracking.ErrorTrackingIssue
import com.nightcode.snout.shared.domain.model.errortracking.IssueRelease
import com.nightcode.snout.shared.domain.model.errortracking.IssueSeverity
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatus
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter
import com.nightcode.snout.shared.domain.model.errortracking.StackFrame
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlin.math.roundToLong

/** PostHog names issues after the exception type; fall back to the message for the rare unnamed one. */
private const val UnnamedIssue = "Unknown error"

internal fun ErrorTrackingIssueDto.toIssue(): ErrorTrackingIssue {
    // List rows carry `aggregations`, the detail response `impact` plus a `sparkline`.
    val counts = aggregations ?: impact
    val title = name.nullIfBlank() ?: description.nullIfBlank() ?: UnnamedIssue
    return ErrorTrackingIssue(
        id = id,
        name = title,
        description = description?.takeIf { it.isNotBlank() && it != title },
        status = status.toIssueStatus(),
        severity = severity.toIssueSeverity(),
        firstSeen = firstSeen,
        lastSeen = lastSeen,
        library = library.nullIfBlank(),
        source = source.nullIfBlank(),
        occurrences = counts?.occurrences.toCount(),
        users = counts?.users.toCount(),
        sessions = counts?.sessions.toCount(),
        volume = (sparkline ?: aggregations?.volumeRange).orEmpty().map { it.roundToLong() },
        topFrame = topInAppFrame?.toStackFrame(),
        latestRelease = latestRelease?.toRelease(),
    )
}

/** Rows of `[version, exceptions]`; rows that don't have that shape are skipped. */
internal fun HogQLResultsDto.toAppVersions(): List<AppVersion> = results.mapNotNull { row ->
    val name = row.getOrNull(0).string().nullIfBlank()
    val exceptions = (row.getOrNull(1) as? JsonPrimitive)?.longOrNull
    if (name != null && exceptions != null) AppVersion(name, exceptions) else null
}

internal val IssueStatusFilter.apiValue: String
    get() = when (this) {
        IssueStatusFilter.Active -> "active"
        IssueStatusFilter.Resolved -> "resolved"
        IssueStatusFilter.Suppressed -> "suppressed"
    }

internal fun String?.toIssueStatus(): IssueStatus = when (this) {
    "active" -> IssueStatus.Active
    "resolved" -> IssueStatus.Resolved
    "suppressed" -> IssueStatus.Suppressed
    "archived" -> IssueStatus.Archived
    "pending_release" -> IssueStatus.PendingRelease
    else -> IssueStatus.Unknown
}

private fun String?.toIssueSeverity(): IssueSeverity? = when (this) {
    "low" -> IssueSeverity.Low
    "medium" -> IssueSeverity.Medium
    "high" -> IssueSeverity.High
    "critical" -> IssueSeverity.Critical
    else -> null
}

private fun Double?.toCount(): Long = this?.roundToLong() ?: 0L

/** PostHog sends an empty object when the issue has no app frame in its latest event. */
private fun ErrorTrackingFrameDto.toStackFrame(): StackFrame? =
    takeIf { function != null || source != null }?.let { StackFrame(function, source, line, column) }

private fun ErrorTrackingReleaseDto.toRelease(): IssueRelease? =
    takeIf { version != null || commitId != null }?.let { IssueRelease(version, commitId, branch) }
