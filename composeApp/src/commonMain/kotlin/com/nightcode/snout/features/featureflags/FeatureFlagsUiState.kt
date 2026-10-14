package com.nightcode.snout.features.featureflags

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.featureflags.FeatureFlag
import com.nightcode.snout.shared.domain.model.featureflags.FeatureFlagStatus
import com.nightcode.snout.shared.domain.model.featureflags.TestFeatureFlag
import com.nightcode.snout.shared.domain.model.featureflags.TestRemoteConfigFeatureFlag

internal data class FeatureFlagsUiState(
    /** The project's flags; null until the first load succeeds. */
    val flags: List<FeatureFlag>? = null,
    /** Why the last load failed; a banner over [flags] once they're loaded. */
    val error: DataError? = null,
    val isRefreshing: Boolean = false,
    val query: String = "",
    /** Flags matching [query], shown instead of [flags] while it isn't blank; null while the search runs. */
    val searchResults: List<FeatureFlag>? = null,
    val searchError: DataError? = null,
)

@TestData
@OptIn(TestData::class)
internal val TestFeatureFlagsUiState = FeatureFlagsUiState(
    flags = listOf(
        TestFeatureFlag,
        TestFeatureFlag.copy(
            id = 8,
            key = "dark-mode",
            description = null,
            active = false,
            status = FeatureFlagStatus.Inactive,
        ),
        TestRemoteConfigFeatureFlag,
    ),
)
