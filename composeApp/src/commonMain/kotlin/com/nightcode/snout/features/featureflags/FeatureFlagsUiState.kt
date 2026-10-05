package com.nightcode.snout.features.featureflags

import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.featureflags.FeatureFlag
import com.nightcode.snout.shared.domain.model.featureflags.FeatureFlagStatus
import com.nightcode.snout.shared.domain.model.featureflags.TestFeatureFlag

internal data class FeatureFlagsUiState(
    val query: String = "",
    val flags: CachedUiState<List<FeatureFlag>> = CachedUiState.Loading,
)

@TestData
@OptIn(TestData::class)
internal val TestFeatureFlagsUiState = FeatureFlagsUiState(
    flags = CachedUiState.Content(
        listOf(
            TestFeatureFlag,
            TestFeatureFlag.copy(
                id = 8,
                key = "dark-mode",
                description = null,
                active = false,
                status = FeatureFlagStatus.Inactive,
            ),
        ),
        isRefreshing = false,
    ),
)
