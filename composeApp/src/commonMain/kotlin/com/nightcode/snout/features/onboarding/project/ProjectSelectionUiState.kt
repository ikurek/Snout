package com.nightcode.snout.features.onboarding.project

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.organization.Organization
import com.nightcode.snout.shared.domain.model.organization.TestOrganization

internal data class ProjectSelectionUiState(
    /** The key's organizations and their projects; null while they load. */
    val organizations: List<Organization>? = null,
    val error: DataError? = null,
)

internal sealed interface ProjectSelectionSideEffect {
    data object ProjectSelected : ProjectSelectionSideEffect
}

@TestData
@OptIn(TestData::class)
internal val TestProjectSelectionUiStateLoaded = ProjectSelectionUiState(organizations = listOf(TestOrganization))
