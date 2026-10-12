package com.nightcode.snout.features.errortracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.RefreshableUiState
import com.nightcode.snout.common.state.liveResults
import com.nightcode.snout.common.state.loadRefreshableList
import com.nightcode.snout.shared.domain.model.errortracking.AppVersion
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ProjectReload
import com.nightcode.snout.shared.domain.usecase.errortracking.GetErrorAppVersions
import com.nightcode.snout.shared.domain.usecase.errortracking.GetErrorTrackingIssues
import com.nightcode.snout.shared.domain.usecase.errortracking.ObserveErrorTrackingReloads
import com.nightcode.snout.shared.domain.usecase.errortracking.SearchErrorTrackingIssues
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/** Filters answered by a live request instead of the base list; [reload] and [refresh] fetch them again. */
private data class LiveFilter(
    val status: IssueStatusFilter,
    val query: String,
    val appVersion: String?,
    val reload: ProjectReload,
    val refresh: Int,
)

@OptIn(FlowPreview::class)
@KoinViewModel
internal class ErrorTrackingViewModel(
    private val searchIssues: SearchErrorTrackingIssues,
    private val getAppVersions: GetErrorAppVersions,
    getIssues: GetErrorTrackingIssues,
    observeErrorTrackingReloads: ObserveErrorTrackingReloads,
    observeSelectedProjectId: ObserveSelectedProjectId,
    searchDebounce: Duration = 300.milliseconds,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val status = MutableStateFlow(IssueStatusFilter.Active)
    private val appVersion = MutableStateFlow<String?>(null)
    private val appVersions = MutableStateFlow<List<AppVersion>>(emptyList())

    /** Pull to refresh for the base list. */
    private val refreshes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** Bumped by pull to refresh while filtered, to fetch the live results again. */
    private val liveRefresh = MutableStateFlow(0)

    // Another status starts from the skeleton; a status changed in the app reloads the list it left.
    private val list = combine(observeErrorTrackingReloads(), status, ::Pair)
        .loadRefreshableList(viewModelScope, refreshes) { (_, status) -> getIssues(status) }

    private val liveResults = combine(
        query.debounce(searchDebounce).distinctUntilChanged(),
        status,
        appVersion,
        observeErrorTrackingReloads(),
        liveRefresh,
    ) { text, status, version, reload, refresh -> LiveFilter(status, text, version, reload, refresh) }
        .liveResults(isLive = { isLive(it.query, it.appVersion) }) { filter ->
            searchIssues(filter.status, filter.query, filter.appVersion)
        }

    private val filters = combine(query, status, appVersion, appVersions) { query, status, version, versions ->
        ErrorTrackingUiState(query = query, status = status, appVersion = version, appVersions = versions)
    }

    val uiState: StateFlow<ErrorTrackingUiState> = combine(filters, list, liveResults) { state, list, live ->
        state.copy(issues = if (isLive(state.query, state.appVersion)) live ?: RefreshableUiState.Loading else list)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ErrorTrackingUiState())

    init {
        viewModelScope.launch {
            // Versions belong to a project, so switching projects drops the version filter.
            observeSelectedProjectId().collectLatest {
                appVersion.value = null
                appVersions.value = emptyList()
                loadAppVersions()
            }
        }
    }

    fun onQueryChanged(text: String) {
        query.value = text
    }

    fun onStatusChanged(value: IssueStatusFilter) {
        status.value = value
    }

    fun onAppVersionChanged(value: String?) {
        appVersion.value = value
    }

    fun refresh() = viewModelScope.launch {
        launch { loadAppVersions() }
        if (isLive(query.value, appVersion.value)) {
            liveRefresh.value++
        } else {
            refreshes.emit(Unit)
        }
    }

    /** A failure only hides the version filter; the issue list reports its own errors. */
    private suspend fun loadAppVersions() {
        getAppVersions().onRight { appVersions.value = it }
    }

    /** Whether the filters need a live request instead of the base list. */
    private fun isLive(query: String, appVersion: String?) = query.isNotBlank() || appVersion != null
}
