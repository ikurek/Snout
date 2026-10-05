package com.nightcode.snout.features.experiments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.common.state.RefreshController
import com.nightcode.snout.common.state.listUiState
import com.nightcode.snout.shared.domain.model.experiments.Experiment
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.experiments.ObserveExperiments
import com.nightcode.snout.shared.domain.usecase.experiments.RefreshExperiments
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class ExperimentsViewModel(
    private val refreshExperiments: RefreshExperiments,
    observeExperiments: ObserveExperiments,
    observeSelectedProjectId: ObserveSelectedProjectId,
) : ViewModel() {

    private val refresh = RefreshController()

    val uiState: StateFlow<CachedUiState<List<Experiment>>> = combine(
        observeExperiments(),
        refresh.status,
    ) { items, status ->
        listUiState(items?.filterNot { it.archived }, status)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CachedUiState.Loading)

    init {
        viewModelScope.launch {
            observeSelectedProjectId().collectLatest { refresh.run { refreshExperiments() } }
        }
    }

    fun refresh() = viewModelScope.launch { refresh.run { refreshExperiments() } }
}
