package com.nightcode.snout.features.featureflags.detail

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.featureflags.FeatureFlag
import com.nightcode.snout.shared.domain.model.featureflags.TestFeatureFlag

/** The switch's write; a failure stays in a banner under the card until dismissed. */
internal sealed interface ToggleStatus {
    data object Idle : ToggleStatus

    data object Updating : ToggleStatus

    data class Failed(val error: DataError) : ToggleStatus
}

internal data class FeatureFlagDetailUiState(
    /** The flag; null until the first load succeeds. */
    val flag: FeatureFlag? = null,
    /** Why the last load failed; a banner over [flag] once it's loaded. */
    val error: DataError? = null,
    val isRefreshing: Boolean = false,
    val toggle: ToggleStatus = ToggleStatus.Idle,
)

@TestData
@OptIn(TestData::class)
internal val TestFeatureFlagDetailUiState = FeatureFlagDetailUiState(
    flag = TestFeatureFlag,
)
