package com.nightcode.snout.features.errortracking.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatus
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter
import com.nightcode.snout.shared.domain.usecase.errortracking.GetErrorTrackingIssue
import com.nightcode.snout.shared.domain.usecase.errortracking.SetErrorTrackingIssueStatus
import com.nightcode.snout.shared.domain.usecase.telemetry.TrackIssueStatusChanged
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class ErrorTrackingIssueViewModel(
    @InjectedParam private val issueId: String,
    private val getIssue: GetErrorTrackingIssue,
    private val setIssueStatus: SetErrorTrackingIssueStatus,
    private val trackIssueStatusChanged: TrackIssueStatusChanged,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ErrorTrackingIssueUiState())
    val uiState: StateFlow<ErrorTrackingIssueUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<ErrorTrackingIssueSideEffect>()
    val sideEffects: Flow<ErrorTrackingIssueSideEffect> = _sideEffects.receiveAsFlow()

    private var loadJob: Job? = null
    private var hasEntered = false

    init {
        load()
    }

    fun refresh() = load()

    /** Refreshes each time the screen is entered again, but not the first time nor while a load runs. */
    fun onScreenEntered() {
        if (hasEntered && loadJob?.isActive != true) refresh()
        hasEntered = true
    }

    /** Reversible, so no confirmation: the snackbar that follows offers to undo. */
    fun changeStatus(target: IssueStatusFilter) {
        val current = _uiState.value
        val issue = current.issue ?: return
        if (current.updatingTo != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(updatingTo = target) }
            setIssueStatus(issueId, target).fold(
                ifLeft = { error ->
                    _uiState.update { it.copy(updatingTo = null) }
                    _sideEffects.send(ErrorTrackingIssueSideEffect.StatusChangeFailed(error))
                },
                ifRight = { status ->
                    _uiState.update {
                        it.copy(
                            issue = issue.copy(status = status),
                            error = null,
                            isRefreshing = false,
                            updatingTo = null,
                        )
                    }
                    trackIssueStatusChanged(status)
                    val undo = issue.status.toFilter()?.takeIf { it != target }
                    _sideEffects.send(ErrorTrackingIssueSideEffect.StatusChanged(target, undo))
                },
            )
        }
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = it.issue != null, error = null) }
            val result = getIssue(issueId)
            _uiState.update { state ->
                result.fold(
                    ifLeft = { state.copy(isRefreshing = false, error = it) },
                    ifRight = { state.copy(issue = it, isRefreshing = false, error = null) },
                )
            }
        }
    }

    private fun IssueStatus.toFilter(): IssueStatusFilter? = when (this) {
        IssueStatus.Active -> IssueStatusFilter.Active
        IssueStatus.Resolved -> IssueStatusFilter.Resolved
        IssueStatus.Suppressed -> IssueStatusFilter.Suppressed
        IssueStatus.Archived, IssueStatus.PendingRelease, IssueStatus.Unknown -> null
    }
}
