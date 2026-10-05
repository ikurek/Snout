package com.nightcode.snout.features.insights

import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.insights.InsightSummary
import com.nightcode.snout.shared.domain.model.insights.TestInsightSummary

internal data class InsightsUiState(
    val query: String = "",
    val insights: CachedUiState<List<InsightSummary>> = CachedUiState.Loading,
)

@TestData
@OptIn(TestData::class)
internal val TestInsightsUiState = InsightsUiState(
    insights = CachedUiState.Content(
        listOf(TestInsightSummary, TestInsightSummary.copy(id = 11, name = "Signups by source", favorited = false)),
        isRefreshing = false,
    ),
)
