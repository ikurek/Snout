package com.nightcode.snout.features.intialproject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.usecase.appstate.SaveSelectedProject
import com.nightcode.snout.shared.domain.usecase.organization.GetOrganizations
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@KoinViewModel
internal class InitialProjectViewModel(
    private val getOrganizations: GetOrganizations,
    private val saveSelectedProject: SaveSelectedProject,
) : ViewModel() {

    private val _state = MutableStateFlow<InitialProjectUiState>(InitialProjectUiState.Loading)
    val state = _state.asStateFlow()

    private val _sideEffects = Channel<InitialProjectSideEffect>()
    val sideEffects: Flow<InitialProjectSideEffect> = _sideEffects.receiveAsFlow()

    init {
        load()
    }

    fun load() = viewModelScope.launch {
        _state.value = InitialProjectUiState.Loading
        getOrganizations().onLeft { error ->
            _state.value = InitialProjectUiState.Error(error)
        }.onRight { organizations ->
            _state.value = InitialProjectUiState.Loaded(organizations)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onProjectSelected(organizationId: Uuid, projectId: Long) = viewModelScope.launch {
        saveSelectedProject(organizationId = organizationId, projectId = projectId)
        _sideEffects.send(InitialProjectSideEffect.ProjectSelected)
    }
}
