package com.nightcode.snout.features.insights.detail

import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.insights.InsightDetail

internal data class InsightDetailUiState(
    /** The insight and its result; null until the first load succeeds. */
    val insight: InsightDetail? = null,
    /** Why the last load failed; a banner over [insight] once it's loaded. */
    val error: DataError? = null,
    val isRefreshing: Boolean = false,
)

internal sealed interface InsightDetailSideEffect {
    data object FavoriteUpdateFailed : InsightDetailSideEffect
}
