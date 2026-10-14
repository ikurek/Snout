package com.nightcode.snout.features.experiments.detail

import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.experiments.Experiment

internal data class ExperimentDetailUiState(
    /** The experiment; null until the first load succeeds. */
    val experiment: Experiment? = null,
    /** Why the last load failed; a banner over [experiment] once it's loaded. */
    val error: DataError? = null,
    val isRefreshing: Boolean = false,
)
