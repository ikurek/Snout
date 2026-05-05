package com.nightcode.snout.features.intialproject

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.organization.GetOrganizationsError
import com.nightcode.snout.shared.domain.model.organization.Organization
import com.nightcode.snout.shared.domain.model.organization.TestOrganization

internal sealed interface InitialProjectUiState {
    data object Loading : InitialProjectUiState

    data class Loaded(val organizations: List<Organization>) : InitialProjectUiState

    data class Error(val error: GetOrganizationsError) : InitialProjectUiState
}

@TestData
@OptIn(TestData::class)
internal val TestInitialProjectUiStateLoaded = InitialProjectUiState.Loaded(
    organizations = listOf(TestOrganization),
)
