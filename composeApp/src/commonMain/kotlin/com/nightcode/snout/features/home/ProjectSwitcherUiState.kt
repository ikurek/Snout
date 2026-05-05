package com.nightcode.snout.features.home

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.appstate.SelectedProject
import com.nightcode.snout.shared.domain.model.appstate.TestSelectedProject
import com.nightcode.snout.shared.domain.model.organization.Organization
import com.nightcode.snout.shared.domain.model.organization.TestOrganization

internal sealed interface ProjectSwitcherUiState {
    val currentProjectName: String

    data object Loading : ProjectSwitcherUiState {
        override val currentProjectName: String = ""
    }

    data class NotFound(override val currentProjectName: String) : ProjectSwitcherUiState

    data class Loaded(
        override val currentProjectName: String,
        val organizations: List<Organization>,
        val selectedProject: SelectedProject?,
    ) : ProjectSwitcherUiState
}

@TestData
@OptIn(TestData::class)
internal val TestProjectSwitcherUiStateLoaded = ProjectSwitcherUiState.Loaded(
    currentProjectName = "My Project",
    organizations = listOf(TestOrganization),
    selectedProject = TestSelectedProject,
)
