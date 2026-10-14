package com.nightcode.snout.features.analytics

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsSnapshot
import com.nightcode.snout.shared.domain.model.analytics.TestAnalyticsSnapshot
import com.nightcode.snout.shared.domain.model.common.DataError

internal data class AnalyticsUiState(
    val range: AnalyticsRange = AnalyticsRange.Last7Days,
    /** The project's figures for [range], as the preferences ask to show them; null until loaded for it. */
    val snapshot: AnalyticsSnapshot? = null,
    /** Why the last load failed; a banner over [snapshot] once it's loaded. */
    val error: DataError? = null,
    val isRefreshing: Boolean = false,
)

@TestData
@OptIn(TestData::class)
internal val TestAnalyticsUiState = AnalyticsUiState(range = AnalyticsRange.Last7Days, snapshot = TestAnalyticsSnapshot)
