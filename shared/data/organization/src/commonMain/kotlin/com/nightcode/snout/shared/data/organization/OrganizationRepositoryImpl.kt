package com.nightcode.snout.shared.data.organization

import arrow.core.Either
import com.nightcode.snout.shared.api.organizations.OrganizationsApi
import com.nightcode.snout.shared.data.organization.mapper.toGetOrganizationsError
import com.nightcode.snout.shared.data.organization.mapper.toOrganization
import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import com.nightcode.snout.shared.domain.model.organization.GetOrganizationsError
import com.nightcode.snout.shared.domain.model.organization.Organization
import com.nightcode.snout.shared.storage.datastore.AuthDataStore

internal class OrganizationRepositoryImpl(
    private val api: OrganizationsApi,
    private val authDataStore: AuthDataStore,
) : OrganizationRepository {

    override suspend fun getOrganizations(): Either<GetOrganizationsError, List<Organization>> = api.getOrganizations()
        .mapLeft { it.toGetOrganizationsError() }
        .map { response -> response.results.map { it.toOrganization(authDataStore.getInstanceUrl()!!) } }
}
