package com.nightcode.snout.shared.domain.data.organization

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.organization.Organization
import com.nightcode.snout.shared.domain.model.organization.OrganizationMember
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface OrganizationRepository {
    suspend fun refreshOrganizations(): Either<DataError, Unit>
    fun observeOrganizations(): Flow<List<Organization>>
    suspend fun clear()

    /** Fetched each time, not kept with the organizations. */
    suspend fun getMembers(organizationId: Uuid): Either<DataError, List<OrganizationMember>>
}
