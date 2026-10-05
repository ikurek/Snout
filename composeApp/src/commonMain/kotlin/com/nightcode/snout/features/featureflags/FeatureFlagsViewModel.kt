package com.nightcode.snout.features.featureflags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.common.state.RefreshController
import com.nightcode.snout.common.state.RefreshStatus
import com.nightcode.snout.common.state.listUiState
import com.nightcode.snout.shared.domain.model.featureflags.FeatureFlag
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.featureflags.ObserveFeatureFlags
import com.nightcode.snout.shared.domain.usecase.featureflags.RefreshFeatureFlags
import com.nightcode.snout.shared.domain.usecase.featureflags.SearchFeatureFlags
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@KoinViewModel
internal class FeatureFlagsViewModel(
    private val refreshFeatureFlags: RefreshFeatureFlags,
    private val searchFeatureFlags: SearchFeatureFlags,
    observeFeatureFlags: ObserveFeatureFlags,
    observeSelectedProjectId: ObserveSelectedProjectId,
    searchDebounce: Duration = 300.milliseconds,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val refresh = RefreshController()

    private val cachedList = combine(
        observeFeatureFlags(),
        refresh.status,
    ) { flags, status -> listUiState(flags, status) }

    private val searchResults: Flow<CachedUiState<List<FeatureFlag>>?> = query
        .debounce(searchDebounce)
        .distinctUntilChanged()
        .flatMapLatest { text -> if (text.isBlank()) flowOf(null) else search(text) }

    val uiState: StateFlow<FeatureFlagsUiState> = combine(query, cachedList, searchResults) { query, cached, searched ->
        FeatureFlagsUiState(query = query, flags = if (query.isBlank()) cached else searched ?: CachedUiState.Loading)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FeatureFlagsUiState())

    init {
        viewModelScope.launch {
            observeSelectedProjectId().collectLatest { refresh.run { refreshFeatureFlags() } }
        }
    }

    fun onQueryChange(text: String) {
        query.value = text
    }

    fun refresh() = viewModelScope.launch { refresh.run { refreshFeatureFlags() } }

    private fun search(text: String): Flow<CachedUiState<List<FeatureFlag>>> = flow {
        emit(CachedUiState.Loading)
        emit(
            searchFeatureFlags(text).fold(
                ifLeft = { CachedUiState.Error(it) },
                ifRight = { listUiState(it, RefreshStatus.Idle) },
            ),
        )
    }
}
