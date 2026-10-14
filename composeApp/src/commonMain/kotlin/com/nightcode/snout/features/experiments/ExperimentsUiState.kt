package com.nightcode.snout.features.experiments

import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.experiments.Experiment

internal data class ExperimentsUiState(
    /** The project's experiments that aren't archived; null until the first load succeeds. */
    val experiments: List<Experiment>? = null,
    /** Why the last load failed; a banner over [experiments] once they're loaded. */
    val error: DataError? = null,
    val isRefreshing: Boolean = false,
)
