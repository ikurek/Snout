package com.nightcode.snout.features.insights.detail

import com.nightcode.snout.common.state.LoadState
import com.nightcode.snout.shared.domain.model.insights.InsightDetail

internal typealias InsightDetailUiState = LoadState<InsightDetail>

internal sealed interface InsightDetailSideEffect {
    data object FavoriteUpdateFailed : InsightDetailSideEffect
}
