package com.nightcode.snout.shared.data.organization

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.nightcode.snout.shared.api.organizations.OrganizationsApi
import com.nightcode.snout.shared.data.organization.mapper.toEntity
import com.nightcode.snout.shared.data.organization.mapper.toOrganization
import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.organization.Organization
import com.nightcode.snout.shared.storage.database.OrganizationsDatabase
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single(binds = [OrganizationRepository::class])
internal class OrganizationRepositoryImpl(
    private val api: OrganizationsApi,
    private val authDataStore: AuthDataStore,
    private val database: OrganizationsDatabase,
) : OrganizationRepository {

    override suspend fun refreshOrganizations(): Either<DataError, Unit> = either {
        val instanceUrl = ensureNotNull(authDataStore.getInstanceUrl()) { DataError.UnexpectedError }
        val organizations = api.getOrganizations()
            .bind()
            .results
            .map { it.toOrganization(instanceUrl) }
        database.replaceAll(organizations.map { it.toEntity() })
    }

    override fun observeOrganizations(): Flow<List<Organization>> =
        database.observe().map { entities -> entities.map { it.toOrganization() } }

    override suspend fun clear() = database.replaceAll(emptyList())
}
