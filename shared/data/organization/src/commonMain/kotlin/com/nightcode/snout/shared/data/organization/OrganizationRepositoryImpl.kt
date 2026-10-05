package com.nightcode.snout.shared.data.organization

import arrow.core.Either
import com.nightcode.snout.shared.api.organizations.OrganizationsApi
import com.nightcode.snout.shared.data.organization.mapper.toEntity
import com.nightcode.snout.shared.data.organization.mapper.toGetOrganizationsError
import com.nightcode.snout.shared.data.organization.mapper.toOrganization
import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import com.nightcode.snout.shared.domain.model.organization.GetOrganizationsError
import com.nightcode.snout.shared.domain.model.organization.Organization
import com.nightcode.snout.shared.storage.database.OrganizationsDatabase
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class OrganizationRepositoryImpl(
    private val api: OrganizationsApi,
    private val authDataStore: AuthDataStore,
    private val database: OrganizationsDatabase,
) : OrganizationRepository {

    override suspend fun refreshOrganizations(): Either<GetOrganizationsError, Unit> = api.getOrganizations()
        .mapLeft { it.toGetOrganizationsError() }
        .map { response -> response.results.map { it.toOrganization(authDataStore.getInstanceUrl()!!) } }
        .onRight { organizations -> database.replaceAll(organizations.map { it.toEntity() }) }
        .map { }

    override fun observeOrganizations(): Flow<List<Organization>> =
        database.observe().map { entities -> entities.map { it.toOrganization() } }

    override suspend fun clear() = database.replaceAll(emptyList())
}
