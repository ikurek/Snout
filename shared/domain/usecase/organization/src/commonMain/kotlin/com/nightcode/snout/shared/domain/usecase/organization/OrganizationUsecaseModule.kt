package com.nightcode.snout.shared.domain.usecase.organization

import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class OrganizationUsecaseModule {
    @Single
    fun provideObserveOrganizations(repository: OrganizationRepository) = ObserveOrganizations(repository)

    @Single
    fun provideRefreshOrganizations(repository: OrganizationRepository) = RefreshOrganizations(repository)
}
