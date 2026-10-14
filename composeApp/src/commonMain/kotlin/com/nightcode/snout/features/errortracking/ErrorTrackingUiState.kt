package com.nightcode.snout.features.errortracking

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.errortracking.AppVersion
import com.nightcode.snout.shared.domain.model.errortracking.ErrorTrackingIssue
import com.nightcode.snout.shared.domain.model.errortracking.IssueSeverity
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter
import com.nightcode.snout.shared.domain.model.errortracking.TestAppVersions
import com.nightcode.snout.shared.domain.model.errortracking.TestErrorTrackingIssue

internal data class ErrorTrackingUiState(
    /** Issues with [status]; null until loaded for it. */
    val issues: List<ErrorTrackingIssue>? = null,
    /** Why the last load failed; a banner over [issues] once they're loaded. */
    val error: DataError? = null,
    val isRefreshing: Boolean = false,
    val query: String = "",
    val status: IssueStatusFilter = IssueStatusFilter.Active,
    /** Selected `$app_version`, or null for every version. */
    val appVersion: String? = null,
    /** Versions to pick from; empty hides the filter, as for web projects that send no app version. */
    val appVersions: List<AppVersion> = emptyList(),
    /** Issues matching [query] and [appVersion], shown instead of [issues] while [isFiltered]; null while loading. */
    val filteredIssues: List<ErrorTrackingIssue>? = null,
    val filterError: DataError? = null,
) {
    val isFiltered: Boolean get() = query.isNotBlank() || appVersion != null
}

@TestData
@OptIn(TestData::class)
internal val TestErrorTrackingUiState = ErrorTrackingUiState(
    appVersions = TestAppVersions,
    issues = listOf(
        TestErrorTrackingIssue,
        TestErrorTrackingIssue.copy(
            id = "01890a4f-6f3c-7c2e-9a1b-3d5e7f9a0b3e",
            name = "ChunkLoadError",
            description = "Loading chunk 412 failed.",
            severity = IssueSeverity.Low,
            occurrences = 87,
            users = 1,
            volume = listOf(0, 0, 12, 40, 21, 9, 5),
        ),
        TestErrorTrackingIssue.copy(
            id = "01890a4f-6f3c-7c2e-9a1b-3d5e7f9a0b4f",
            name = "IllegalStateException",
            description = "Fragment not attached to a context.",
            severity = null,
            library = "posthog-android",
            occurrences = 3,
            users = 2,
            volume = listOf(1, 0, 0, 1, 0, 0, 1),
        ),
    ),
)
