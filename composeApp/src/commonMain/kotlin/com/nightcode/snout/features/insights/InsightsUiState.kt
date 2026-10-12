package com.nightcode.snout.features.insights

import com.nightcode.snout.common.state.RefreshableUiState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.insights.InsightSummary
import com.nightcode.snout.shared.domain.model.insights.TestInsightSummary

internal sealed interface InsightsSideEffect {
    data object FavoriteUpdateFailed : InsightsSideEffect
}

internal data class InsightsUiState(
    val query: String = "",
    val insights: RefreshableUiState<List<InsightSummary>> = RefreshableUiState.Loading,
)

@TestData
@OptIn(TestData::class)
internal val TestInsightsUiState = InsightsUiState(
    insights = RefreshableUiState.Content(
        listOf(TestInsightSummary, TestInsightSummary.copy(id = 11, name = "Signups by source", favorited = false)),
        isRefreshing = false,
    ),
)
