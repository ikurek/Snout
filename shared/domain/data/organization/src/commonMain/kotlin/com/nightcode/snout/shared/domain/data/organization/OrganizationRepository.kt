package com.nightcode.snout.shared.domain.data.organization

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.organization.Organization
import kotlinx.coroutines.flow.Flow

interface OrganizationRepository {
    suspend fun refreshOrganizations(): Either<DataError, Unit>
    fun observeOrganizations(): Flow<List<Organization>>
    suspend fun clear()
}
