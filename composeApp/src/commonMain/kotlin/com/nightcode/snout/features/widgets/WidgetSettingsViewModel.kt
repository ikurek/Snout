package com.nightcode.snout.features.widgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.usecase.appstate.IsSignedIn
import com.nightcode.snout.widgets.WidgetProjects
import com.nightcode.snout.widgets.WidgetState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

/** The projects the settings of an Android analytics or highlights widget offer; the rest is the screen's own state. */
@KoinViewModel
internal class WidgetSettingsViewModel(
    @InjectedParam private val widget: WidgetProject,
    private val isSignedIn: IsSignedIn,
    private val widgetProjects: WidgetProjects,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WidgetSettingsUiState())
    val uiState: StateFlow<WidgetSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val isConnected = isSignedIn()
            _uiState.update { it.copy(isConnected = isConnected) }
            if (!isConnected) return@launch
            // A new widget starts on no project: the person picks it.
            _uiState.update { it.copy(projectId = widget.projectId) }
            loadProjects()
        }
    }

    fun onProjectSelected(projectId: Long) {
        _uiState.update { it.copy(projectId = projectId) }
    }

    /** Loads the projects again, after they failed to. */
    fun retryProjects() {
        viewModelScope.launch { loadProjects() }
    }

    private suspend fun loadProjects() {
        _uiState.update { it.copy(projectsError = null) }
        val projects = widgetProjects.projects()
        _uiState.update {
            when (projects) {
                is WidgetState.Loaded -> it.copy(projects = projects.content)

                is WidgetState.Failed -> it.copy(projectsError = projects.error)

                // Signed out since the screen opened: nothing to pick, as when it opens signed out.
                WidgetState.SignedOut -> it.copy(isConnected = false)
            }
        }
    }
}
