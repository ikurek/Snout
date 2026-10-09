package com.nightcode.snout.features.errortracking.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatus
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter
import com.nightcode.snout.shared.domain.usecase.errortracking.GetErrorTrackingIssue
import com.nightcode.snout.shared.domain.usecase.errortracking.SetErrorTrackingIssueStatus
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(ErrorTrackingIssueUiState())
    val uiState: StateFlow<ErrorTrackingIssueUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<ErrorTrackingIssueSideEffect>()
    val sideEffects: Flow<ErrorTrackingIssueSideEffect> = _sideEffects.receiveAsFlow()

    init {
        load()
    }

    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(issue = CachedUiState.Loading) }
        val issue = getIssue(issueId).fold(
            ifLeft = { CachedUiState.Error(it) },
            ifRight = { CachedUiState.Content(it, isRefreshing = false) },
        )
        _uiState.update { it.copy(issue = issue) }
    }

    /** Reversible, so no confirmation: the snackbar that follows offers to undo. */
    fun onStatusChange(target: IssueStatusFilter) {
        val current = _uiState.value
        val issue = (current.issue as? CachedUiState.Content)?.data ?: return
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
                            issue = CachedUiState.Content(issue.copy(status = status), isRefreshing = false),
                            updatingTo = null,
                        )
                    }
                    val undo = issue.status.toFilter()?.takeIf { it != target }
                    _sideEffects.send(ErrorTrackingIssueSideEffect.StatusChanged(target, undo))
                },
            )
        }
    }

    private fun IssueStatus.toFilter(): IssueStatusFilter? = when (this) {
        IssueStatus.Active -> IssueStatusFilter.Active
        IssueStatus.Resolved -> IssueStatusFilter.Resolved
        IssueStatus.Suppressed -> IssueStatusFilter.Suppressed
        IssueStatus.Archived, IssueStatus.PendingRelease, IssueStatus.Unknown -> null
    }
}
