package com.nightcode.snout.shared.data.organization

import com.nightcode.snout.shared.api.organizations.OrganizationsApi
import com.nightcode.snout.shared.api.organizations.OrganizationsApiModule
import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import com.nightcode.snout.shared.storage.datastore.DataStoreModule
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [OrganizationsApiModule::class, DataStoreModule::class])
class OrganizationDataModule {
    @Single(binds = [OrganizationRepository::class])
    fun provideOrganizationRepository(api: OrganizationsApi, authDataStore: AuthDataStore): OrganizationRepository =
        OrganizationRepositoryImpl(api, authDataStore)
}
