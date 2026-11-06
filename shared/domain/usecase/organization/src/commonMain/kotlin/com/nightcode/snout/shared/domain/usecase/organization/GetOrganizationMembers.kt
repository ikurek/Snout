package com.nightcode.snout.shared.domain.usecase.organization

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.organization.OrganizationMember
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProject
import org.koin.core.annotation.Single

/** Members of the selected project's organization, by name. */
@Single
class GetOrganizationMembers(
    private val getSelectedProject: GetSelectedProject,
    private val repository: OrganizationRepository,
) {
    suspend operator fun invoke(): Either<DataError, List<OrganizationMember>> {
        val organizationId = checkNotNull(getSelectedProject()) { "No project selected" }.organizationId
        return repository.getMembers(organizationId)
    }
}
