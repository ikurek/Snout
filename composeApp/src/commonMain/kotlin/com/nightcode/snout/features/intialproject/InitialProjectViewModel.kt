package com.nightcode.snout.features.intialproject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.model.organization.GetOrganizationsError
import com.nightcode.snout.shared.domain.usecase.appstate.SaveSelectedProject
import com.nightcode.snout.shared.domain.usecase.organization.ObserveOrganizations
import com.nightcode.snout.shared.domain.usecase.organization.RefreshOrganizations
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@KoinViewModel
internal class InitialProjectViewModel(
    private val observeOrganizations: ObserveOrganizations,
    private val refreshOrganizations: RefreshOrganizations,
    private val saveSelectedProject: SaveSelectedProject,
) : ViewModel() {

    private val _sideEffects = Channel<InitialProjectSideEffect>()
    val sideEffects: Flow<InitialProjectSideEffect> = _sideEffects.receiveAsFlow()

    private val isLoading = MutableStateFlow(true)

    private val error = MutableStateFlow<GetOrganizationsError?>(null)

    val state = combine(
        observeOrganizations(),
        isLoading,
        error,
    ) { organizations, isLoading, error ->
        when {
            isLoading -> InitialProjectUiState.Loading
            error != null -> InitialProjectUiState.Error(error)
            else -> InitialProjectUiState.Loaded(organizations)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        InitialProjectUiState.Loading,
    )

    init {
        load()
    }

    fun load() = viewModelScope.launch {
        isLoading.value = true
        refreshOrganizations()
            .onLeft { error.value = it }
            .onRight { error.value = null }
        isLoading.value = false
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onProjectSelected(organizationId: Uuid, projectId: Long) = viewModelScope.launch {
        saveSelectedProject(organizationId = organizationId, projectId = projectId)
        _sideEffects.send(InitialProjectSideEffect.ProjectSelected)
    }
}
