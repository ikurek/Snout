package com.nightcode.snout.features.projectswitcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.model.organization.Organization
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProject
import com.nightcode.snout.shared.domain.usecase.appstate.SaveSelectedProject
import com.nightcode.snout.shared.domain.usecase.organization.ObserveOrganizations
import com.nightcode.snout.shared.domain.usecase.organization.RefreshOrganizations
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class ProjectSwitcherViewModel(
    private val observeOrganizations: ObserveOrganizations,
    refreshOrganizations: RefreshOrganizations,
    private val observeSelectedProject: ObserveSelectedProject,
    private val saveSelectedProject: SaveSelectedProject,
) : ViewModel() {

    val uiState: StateFlow<ProjectSwitcherUiState> = combine(
        observeOrganizations(),
        // Home and Settings, the only places with a switcher, are reached only once a project is selected.
        observeSelectedProject().filterNotNull(),
    ) { organizations, selected ->
        val currentProject = organizations.findProject(
            organizationId = selected.organizationId,
            projectId = selected.projectId,
        )

        if (organizations.isEmpty()) {
            ProjectSwitcherUiState.NotFound(currentProjectName = currentProject?.name.orEmpty())
        } else {
            ProjectSwitcherUiState.Loaded(
                currentProjectName = currentProject?.name.orEmpty(),
                organizations = organizations,
                selectedProject = selected,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProjectSwitcherUiState.Loading,
    )

    init {
        // The stored list shows meanwhile; a failed refresh just leaves it.
        viewModelScope.launch { refreshOrganizations() }
    }

    fun onProjectSelected(organizationId: Uuid, projectId: Long) = viewModelScope.launch {
        saveSelectedProject(organizationId = organizationId, projectId = projectId)
    }
}

private fun List<Organization>.findProject(organizationId: Uuid, projectId: Long) =
    firstOrNull { it.id == organizationId }
        ?.projects
        ?.firstOrNull { it.id == projectId }
