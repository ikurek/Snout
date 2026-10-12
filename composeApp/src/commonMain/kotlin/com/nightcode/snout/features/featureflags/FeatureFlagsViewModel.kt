package com.nightcode.snout.features.featureflags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.RefreshableUiState
import com.nightcode.snout.common.state.loadRefreshableList
import com.nightcode.snout.common.state.searchResults
import com.nightcode.snout.shared.domain.usecase.featureflags.GetFeatureFlags
import com.nightcode.snout.shared.domain.usecase.featureflags.ObserveFeatureFlagReloads
import com.nightcode.snout.shared.domain.usecase.featureflags.SearchFeatureFlags
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.KoinViewModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@KoinViewModel
internal class FeatureFlagsViewModel(
    private val searchFeatureFlags: SearchFeatureFlags,
    getFeatureFlags: GetFeatureFlags,
    observeFeatureFlagReloads: ObserveFeatureFlagReloads,
    searchDebounce: Duration = 300.milliseconds,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val refreshes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val list = observeFeatureFlagReloads().loadRefreshableList(viewModelScope, refreshes) { getFeatureFlags() }

    private val searchResults = query.searchResults(searchDebounce) { searchFeatureFlags(it) }

    val uiState: StateFlow<FeatureFlagsUiState> = combine(query, list, searchResults) { query, list, searched ->
        FeatureFlagsUiState(
            query = query,
            flags = if (query.isBlank()) list else searched ?: RefreshableUiState.Loading,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FeatureFlagsUiState())

    fun onQueryChanged(text: String) {
        query.value = text
    }

    fun refresh() {
        refreshes.tryEmit(Unit)
    }
}
