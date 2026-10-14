package com.nightcode.snout.features.insights

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.insights.InsightSummary
import com.nightcode.snout.shared.domain.model.insights.TestInsightSummary

internal sealed interface InsightsSideEffect {
    data object FavoriteUpdateFailed : InsightsSideEffect
}

internal data class InsightsUiState(
    /** The project's insights; null until the first load succeeds. */
    val insights: List<InsightSummary>? = null,
    /** Why the last load failed; a banner over [insights] once they're loaded. */
    val error: DataError? = null,
    val isRefreshing: Boolean = false,
    val query: String = "",
    /** Insights matching [query], shown instead of [insights] while it isn't blank; null while the search runs. */
    val searchResults: List<InsightSummary>? = null,
    val searchError: DataError? = null,
)

@TestData
@OptIn(TestData::class)
internal val TestInsightsUiState = InsightsUiState(
    insights = listOf(
        TestInsightSummary,
        TestInsightSummary.copy(id = 11, name = "Signups by source", favorited = false),
    ),
)
