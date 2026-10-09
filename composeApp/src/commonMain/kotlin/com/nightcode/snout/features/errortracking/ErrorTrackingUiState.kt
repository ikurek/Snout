package com.nightcode.snout.features.errortracking

import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.errortracking.AppVersion
import com.nightcode.snout.shared.domain.model.errortracking.ErrorTrackingIssue
import com.nightcode.snout.shared.domain.model.errortracking.IssueSeverity
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter
import com.nightcode.snout.shared.domain.model.errortracking.TestAppVersions
import com.nightcode.snout.shared.domain.model.errortracking.TestErrorTrackingIssue

internal data class ErrorTrackingUiState(
    val query: String = "",
    val status: IssueStatusFilter = IssueStatusFilter.Active,
    /** Selected `$app_version`, or null for every version. */
    val appVersion: String? = null,
    /** Versions to pick from; empty hides the filter, as for web projects that send no app version. */
    val appVersions: List<AppVersion> = emptyList(),
    val issues: CachedUiState<List<ErrorTrackingIssue>> = CachedUiState.Loading,
)

@TestData
@OptIn(TestData::class)
internal val TestErrorTrackingUiState = ErrorTrackingUiState(
    appVersions = TestAppVersions,
    issues = CachedUiState.Content(
        listOf(
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
        isRefreshing = false,
    ),
)
