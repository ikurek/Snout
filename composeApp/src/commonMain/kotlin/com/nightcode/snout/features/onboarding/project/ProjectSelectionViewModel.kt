package com.nightcode.snout.features.onboarding.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.appstate.SaveSelectedProject
import com.nightcode.snout.shared.domain.usecase.auth.Logout
import com.nightcode.snout.shared.domain.usecase.organization.ObserveOrganizations
import com.nightcode.snout.shared.domain.usecase.organization.RefreshOrganizations
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class ProjectSelectionViewModel(
    private val observeOrganizations: ObserveOrganizations,
    private val refreshOrganizations: RefreshOrganizations,
    private val saveSelectedProject: SaveSelectedProject,
    private val logout: Logout,
) : ViewModel() {

    private val _sideEffects = Channel<ProjectSelectionSideEffect>()
    val sideEffects: Flow<ProjectSelectionSideEffect> = _sideEffects.receiveAsFlow()

    private val isLoading = MutableStateFlow(true)

    private val error = MutableStateFlow<DataError?>(null)

    val uiState: StateFlow<ProjectSelectionUiState> = combine(
        observeOrganizations(),
        isLoading,
        error,
    ) { organizations, isLoading, error ->
        when {
            isLoading -> ProjectSelectionUiState()
            error != null -> ProjectSelectionUiState(error = error)
            else -> ProjectSelectionUiState(organizations = organizations)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ProjectSelectionUiState(),
    )

    /** Called on every arrival at this step: the key may have changed since the last visit. */
    fun refresh() = viewModelScope.launch {
        isLoading.value = true
        refreshOrganizations()
            .onLeft { error.value = it }
            .onRight { error.value = null }
        isLoading.value = false
    }

    /** Leaving this step undoes the connection, so the login step can switch key or instance. */
    fun disconnect() = viewModelScope.launch { logout() }

    fun onProjectSelected(organizationId: Uuid, projectId: Long) = viewModelScope.launch {
        saveSelectedProject(organizationId = organizationId, projectId = projectId)
        _sideEffects.send(ProjectSelectionSideEffect.ProjectSelected)
    }
}
