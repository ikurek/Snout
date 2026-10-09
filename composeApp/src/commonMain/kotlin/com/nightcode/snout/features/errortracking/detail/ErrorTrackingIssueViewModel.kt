package com.nightcode.snout.features.errortracking.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.shared.domain.model.errortracking.ErrorTrackingIssue
import com.nightcode.snout.shared.domain.usecase.errortracking.GetErrorTrackingIssue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class ErrorTrackingIssueViewModel(
    @InjectedParam private val issueId: String,
    private val getIssue: GetErrorTrackingIssue,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CachedUiState<ErrorTrackingIssue>>(CachedUiState.Loading)
    val uiState: StateFlow<CachedUiState<ErrorTrackingIssue>> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() = viewModelScope.launch {
        _uiState.value = CachedUiState.Loading
        _uiState.value = getIssue(issueId).fold(
            ifLeft = { CachedUiState.Error(it) },
            ifRight = { CachedUiState.Content(it, isRefreshing = false) },
        )
    }
}
