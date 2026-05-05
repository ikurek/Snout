package com.nightcode.snout.shared.domain.usecase.organization

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import com.nightcode.snout.shared.domain.model.organization.GetOrganizationsError

class RefreshOrganizations(private val repository: OrganizationRepository) {
    suspend operator fun invoke(): Either<GetOrganizationsError, Unit> = repository.refreshOrganizations()
}
