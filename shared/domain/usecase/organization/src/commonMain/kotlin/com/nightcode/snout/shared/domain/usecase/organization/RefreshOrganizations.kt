package com.nightcode.snout.shared.domain.usecase.organization

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import org.koin.core.annotation.Single

@Single
class RefreshOrganizations(private val repository: OrganizationRepository) {
    suspend operator fun invoke(): Either<DataError, Unit> = repository.refreshOrganizations()
}
