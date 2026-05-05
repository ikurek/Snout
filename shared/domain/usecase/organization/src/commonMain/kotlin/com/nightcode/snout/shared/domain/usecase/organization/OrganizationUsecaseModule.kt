package com.nightcode.snout.shared.domain.usecase.organization

import com.nightcode.snout.shared.data.organization.OrganizationDataModule
import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [OrganizationDataModule::class])
class OrganizationUsecaseModule {
    @Single
    fun observeOrganizations(repository: OrganizationRepository) = ObserveOrganizations(repository)

    @Single
    fun refreshOrganizations(repository: OrganizationRepository) = RefreshOrganizations(repository)
}
