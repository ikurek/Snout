package com.nightcode.snout.features.errortracking.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.model.errortracking.IssueAssignee
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatus
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter
import com.nightcode.snout.shared.domain.usecase.errortracking.AssignErrorTrackingIssue
import com.nightcode.snout.shared.domain.usecase.errortracking.GetErrorTrackingIssue
import com.nightcode.snout.shared.domain.usecase.errortracking.GetIssueOccurrences
import com.nightcode.snout.shared.domain.usecase.errortracking.GetOccurrenceExceptions
import com.nightcode.snout.shared.domain.usecase.errortracking.SetErrorTrackingIssueStatus
import com.nightcode.snout.shared.domain.usecase.organization.GetOrganizationMembers
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

@Suppress("TooManyFunctions", "LongParameterList")
@KoinViewModel
internal class ErrorTrackingIssueViewModel(
    @InjectedParam private val issueId: String,
    private val getIssue: GetErrorTrackingIssue,
    private val setIssueStatus: SetErrorTrackingIssueStatus,
    private val getOccurrences: GetIssueOccurrences,
    private val getOccurrenceExceptions: GetOccurrenceExceptions,
    private val assignIssue: AssignErrorTrackingIssue,
    private val getMembers: GetOrganizationMembers,
    private val trackIssueStatusChanged: TrackIssueStatusChanged,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ErrorTrackingIssueUiState())
    val uiState: StateFlow<ErrorTrackingIssueUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<ErrorTrackingIssueSideEffect>()
    val sideEffects: Flow<ErrorTrackingIssueSideEffect> = _sideEffects.receiveAsFlow()

    private var loadJob: Job? = null
    private var exceptionsJob: Job? = null
    private var membersJob: Job? = null
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
                            issue = it.issue?.copy(status = status),
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

    fun onOccurrenceSelected(occurrenceId: String) {
        if (occurrenceId == _uiState.value.selectedOccurrenceId) return
        _uiState.update { it.copy(selectedOccurrenceId = occurrenceId) }
        loadExceptions()
    }

    fun onExceptionsRetried() = loadExceptions()

    /**
     * Opens the assignee picker once the members are loaded. When they failed, says why and loads them again, so
     * the next tap can open it.
     */
    fun onAssigneeClicked() {
        val state = _uiState.value
        if (state.issue == null || state.isAssigning) return
        val membersError = state.membersError
        if (membersError != null && state.members == null) {
            viewModelScope.launch { _sideEffects.send(ErrorTrackingIssueSideEffect.MembersUnavailable(membersError)) }
            loadMembers()
        } else {
            _uiState.update { it.copy(isAssigneePickerRequested = true) }
        }
    }

    fun onAssigneePickerDismissed() = _uiState.update { it.copy(isAssigneePickerRequested = false) }

    fun onAssigneeSelected(userId: Long?) {
        val current = _uiState.value
        val previous = current.issue?.assignee
        val target = userId?.let(IssueAssignee::User)
        if (current.issue == null || current.isAssigning || previous == target) {
            _uiState.update { it.copy(isAssigneePickerRequested = false) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isAssigning = true, isAssigneePickerRequested = false) }
            assignIssue(issueId, userId).fold(
                ifLeft = { error ->
                    _uiState.update { it.copy(isAssigning = false) }
                    _sideEffects.send(ErrorTrackingIssueSideEffect.AssignFailed(error))
                },
                ifRight = {
                    _uiState.update { it.copy(issue = it.issue?.copy(assignee = target), isAssigning = false) }
                    val name = userId?.let { id -> _uiState.value.members?.firstOrNull { it.userId == id }?.name }
                    val undo = when (previous) {
                        null -> AssigneeUndo(userId = null)
                        is IssueAssignee.User -> AssigneeUndo(userId = previous.userId)
                        is IssueAssignee.Role -> null
                    }
                    _sideEffects.send(ErrorTrackingIssueSideEffect.AssigneeChanged(userId, name, undo))
                },
            )
        }
    }

    /** The issue, its occurrences and the members load side by side, so the issue shows without waiting. */
    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = it.issue != null, error = null) }
            launch { loadOccurrences() }
            if (_uiState.value.members == null) loadMembers()
            val result = getIssue(issueId)
            _uiState.update { state ->
                result.fold(
                    ifLeft = { state.copy(isRefreshing = false, error = it) },
                    ifRight = { state.copy(issue = it, isRefreshing = false, error = null) },
                )
            }
        }
    }

    /**
     * Keeps the picked occurrence when it is still among the latest, else shows the newest. The exceptions of an
     * event never change, so they load again only for another occurrence or after they failed.
     */
    private suspend fun loadOccurrences() {
        getOccurrences(issueId).fold(
            ifLeft = { error -> _uiState.update { it.copy(occurrencesError = error) } },
            ifRight = { occurrences ->
                val before = _uiState.value
                val selected = before.selectedOccurrenceId?.takeIf { id -> occurrences.any { it.id == id } }
                    ?: occurrences.firstOrNull()?.id
                _uiState.update {
                    it.copy(occurrences = occurrences, occurrencesError = null, selectedOccurrenceId = selected)
                }
                if (selected != before.selectedOccurrenceId || before.exceptions == null) loadExceptions()
            },
        )
    }

    private fun loadExceptions() {
        exceptionsJob?.cancel()
        val occurrence = _uiState.value.selectedOccurrence
        _uiState.update { it.copy(exceptions = null, exceptionsError = null) }
        if (occurrence == null) return
        exceptionsJob = viewModelScope.launch {
            getOccurrenceExceptions(occurrence).fold(
                ifLeft = { error -> _uiState.update { it.copy(exceptionsError = error) } },
                ifRight = { exceptions -> _uiState.update { it.copy(exceptions = exceptions) } },
            )
        }
    }

    private fun loadMembers() {
        if (membersJob?.isActive == true) return
        membersJob = viewModelScope.launch {
            _uiState.update { it.copy(membersError = null) }
            getMembers().fold(
                ifLeft = { error -> _uiState.update { it.copy(membersError = error) } },
                ifRight = { members -> _uiState.update { it.copy(members = members, membersError = null) } },
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
