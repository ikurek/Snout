package com.nightcode.snout.shared.domain.usecase.organization

import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import com.nightcode.snout.shared.domain.model.organization.Organization
import kotlinx.coroutines.flow.Flow

class ObserveOrganizations(private val repository: OrganizationRepository) {
    operator fun invoke(): Flow<List<Organization>> = repository.observeOrganizations()
}
