package com.nightcode.snout.features.featureflags.detail

import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.featureflags.FeatureFlag
import com.nightcode.snout.shared.domain.model.featureflags.TestFeatureFlag

internal sealed interface ToggleStatus {
    data object Idle : ToggleStatus

    data object Updating : ToggleStatus

    data class Failed(val error: DataError) : ToggleStatus
}

internal data class FeatureFlagDetailUiState(
    val flag: CachedUiState<FeatureFlag> = CachedUiState.Loading,
    val toggle: ToggleStatus = ToggleStatus.Idle,
)

@TestData
@OptIn(TestData::class)
internal val TestFeatureFlagDetailUiState = FeatureFlagDetailUiState(
    flag = CachedUiState.Content(TestFeatureFlag, isRefreshing = false),
)
