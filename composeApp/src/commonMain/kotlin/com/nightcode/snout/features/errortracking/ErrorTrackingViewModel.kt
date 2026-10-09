package com.nightcode.snout.features.errortracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.common.state.RefreshController
import com.nightcode.snout.common.state.RefreshStatus
import com.nightcode.snout.common.state.listUiState
import com.nightcode.snout.shared.domain.model.errortracking.AppVersion
import com.nightcode.snout.shared.domain.model.errortracking.ErrorTrackingIssue
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.errortracking.GetErrorAppVersions
import com.nightcode.snout.shared.domain.usecase.errortracking.ObserveErrorTrackingIssues
import com.nightcode.snout.shared.domain.usecase.errortracking.RefreshErrorTrackingIssues
import com.nightcode.snout.shared.domain.usecase.errortracking.SearchErrorTrackingIssues
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

/** Filters answered by a live request instead of the cached list. */
private data class LiveFilter(
    val status: IssueStatusFilter,
    val query: String,
    val appVersion: String?,
    val reload: Int,
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@KoinViewModel
internal class ErrorTrackingViewModel(
    private val refreshIssues: RefreshErrorTrackingIssues,
    private val searchIssues: SearchErrorTrackingIssues,
    private val getAppVersions: GetErrorAppVersions,
    observeIssues: ObserveErrorTrackingIssues,
    observeSelectedProjectId: ObserveSelectedProjectId,
    searchDebounce: Duration = 300.milliseconds,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val status = MutableStateFlow(IssueStatusFilter.Active)
    private val appVersion = MutableStateFlow<String?>(null)
    private val appVersions = MutableStateFlow<List<AppVersion>>(emptyList())

    /** Bumped by pull to refresh while filtered, since live results have no cache to refresh. */
    private val liveReload = MutableStateFlow(0)
    private val refresh = RefreshController()

    // Each status has its own cache entry, so switching shows the last known list while it refreshes.
    private val cachedList = combine(
        status.flatMapLatest { observeIssues(it) },
        refresh.status,
    ) { issues, refreshStatus -> listUiState(issues, refreshStatus) }

    private val liveResults: Flow<CachedUiState<List<ErrorTrackingIssue>>?> = combine(
        query.debounce(searchDebounce).distinctUntilChanged(),
        status,
        appVersion,
        liveReload,
    ) { text, status, version, reload -> LiveFilter(status, text, version, reload) }
        .flatMapLatest { filter -> if (filter.isLive) fetch(filter) else flowOf(null) }

    private val filters = combine(query, status, appVersion, appVersions) { query, status, version, versions ->
        ErrorTrackingUiState(query = query, status = status, appVersion = version, appVersions = versions)
    }

    val uiState: StateFlow<ErrorTrackingUiState> = combine(filters, cachedList, liveResults) { state, cached, live ->
        val isLive = state.query.isNotBlank() || state.appVersion != null
        state.copy(issues = if (isLive) live ?: CachedUiState.Loading else cached)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ErrorTrackingUiState())

    init {
        viewModelScope.launch {
            combine(observeSelectedProjectId(), status) { _, status -> status }
                .collectLatest { status -> refresh.run { refreshIssues(status) } }
        }
        viewModelScope.launch {
            // Versions belong to a project, so switching projects drops the version filter.
            observeSelectedProjectId().collectLatest {
                appVersion.value = null
                appVersions.value = emptyList()
                loadAppVersions()
            }
        }
    }

    fun onQueryChange(text: String) {
        query.value = text
    }

    fun onStatusChange(value: IssueStatusFilter) {
        status.value = value
    }

    fun onAppVersionChange(value: String?) {
        appVersion.value = value
    }

    fun refresh() = viewModelScope.launch {
        launch { loadAppVersions() }
        if (query.value.isNotBlank() || appVersion.value != null) {
            liveReload.value++
        } else {
            refresh.run { refreshIssues(status.value) }
        }
    }

    /** A failure only hides the version filter; the issue list reports its own errors. */
    private suspend fun loadAppVersions() {
        getAppVersions().onRight { appVersions.value = it }
    }

    private val LiveFilter.isLive: Boolean get() = query.isNotBlank() || appVersion != null

    private fun fetch(filter: LiveFilter): Flow<CachedUiState<List<ErrorTrackingIssue>>> = flow {
        emit(CachedUiState.Loading)
        emit(
            searchIssues(filter.status, filter.query, filter.appVersion).fold(
                ifLeft = { CachedUiState.Error(it) },
                ifRight = { listUiState(it, RefreshStatus.Idle) },
            ),
        )
    }
}
