package com.nightcode.snout.features.onboarding.project

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.organization.Organization
import com.nightcode.snout.shared.domain.model.organization.TestOrganization

internal sealed interface ProjectSelectionUiState {
    data object Loading : ProjectSelectionUiState

    data class Loaded(val organizations: List<Organization>) : ProjectSelectionUiState

    data class Error(val error: DataError) : ProjectSelectionUiState
}

@TestData
@OptIn(TestData::class)
internal val TestProjectSelectionUiStateLoaded = ProjectSelectionUiState.Loaded(
    organizations = listOf(TestOrganization),
)
