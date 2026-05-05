package com.nightcode.snout.shared.domain.data.organization

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.organization.GetOrganizationsError
import com.nightcode.snout.shared.domain.model.organization.Organization
import kotlinx.coroutines.flow.Flow

interface OrganizationRepository {
    suspend fun refreshOrganizations(): Either<GetOrganizationsError, Unit>
    fun observeOrganizations(): Flow<List<Organization>>
}
