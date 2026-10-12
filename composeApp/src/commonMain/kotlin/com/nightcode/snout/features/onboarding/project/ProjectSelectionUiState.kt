package com.nightcode.snout.features.onboarding.project

import com.nightcode.snout.common.state.LoadState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.organization.Organization
import com.nightcode.snout.shared.domain.model.organization.TestOrganization

internal typealias ProjectSelectionUiState = LoadState<List<Organization>>

internal sealed interface ProjectSelectionSideEffect {
    data object ProjectSelected : ProjectSelectionSideEffect
}

@TestData
@OptIn(TestData::class)
internal val TestProjectSelectionUiStateLoaded: ProjectSelectionUiState = LoadState.Loaded(
    listOf(TestOrganization),
)
