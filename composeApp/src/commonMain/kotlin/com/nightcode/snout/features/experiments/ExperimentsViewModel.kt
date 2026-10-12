package com.nightcode.snout.features.experiments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.RefreshableUiState
import com.nightcode.snout.common.state.loadRefreshableList
import com.nightcode.snout.shared.domain.model.experiments.Experiment
import com.nightcode.snout.shared.domain.usecase.experiments.GetExperiments
import com.nightcode.snout.shared.domain.usecase.experiments.ObserveExperimentReloads
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class ExperimentsViewModel(
    getExperiments: GetExperiments,
    observeExperimentReloads: ObserveExperimentReloads,
) : ViewModel() {

    private val refreshes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val uiState: StateFlow<RefreshableUiState<List<Experiment>>> = observeExperimentReloads()
        .loadRefreshableList(
            viewModelScope,
            refreshes,
        ) { getExperiments().map { items -> items.filterNot { it.archived } } }

    fun refresh() {
        refreshes.tryEmit(Unit)
    }
}
