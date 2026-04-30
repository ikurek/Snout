package com.nightcode.snout.shared.domain.data.organization

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.organization.GetOrganizationsError
import com.nightcode.snout.shared.domain.model.organization.Organization

interface OrganizationRepository {
    suspend fun getOrganizations(): Either<GetOrganizationsError, List<Organization>>
}
