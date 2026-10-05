package com.nightcode.snout.features.analytics

import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsSnapshot
import com.nightcode.snout.shared.domain.model.analytics.TestAnalyticsSnapshot

internal data class AnalyticsUiState(
    val range: AnalyticsRange = AnalyticsRange.Last7Days,
    val snapshot: CachedUiState<AnalyticsSnapshot> = CachedUiState.Loading,
)

@TestData
@OptIn(TestData::class)
internal val TestAnalyticsUiState = AnalyticsUiState(
    range = AnalyticsRange.Last7Days,
    snapshot = CachedUiState.Content(TestAnalyticsSnapshot, isRefreshing = false),
)
