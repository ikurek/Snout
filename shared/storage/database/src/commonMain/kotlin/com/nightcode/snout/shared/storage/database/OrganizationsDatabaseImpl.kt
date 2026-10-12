package com.nightcode.snout.shared.storage.database

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.nightcode.snout.shared.common.coroutines.IoDispatcher
import com.nightcode.snout.shared.storage.database.entity.OrganizationEntity
import com.nightcode.snout.shared.storage.database.entity.ProjectEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

@Single(binds = [OrganizationsDatabase::class])
internal class OrganizationsDatabaseImpl(
    private val database: SnoutDatabase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : OrganizationsDatabase {

    override fun observe(): Flow<List<OrganizationEntity>> {
        val orgsFlow = database.organizationQueries
            .selectAll()
            .asFlow()
            .mapToList(ioDispatcher)

        val projectsFlow = database.projectQueries
            .selectAll()
            .asFlow()
            .mapToList(ioDispatcher)

        return combine(orgsFlow, projectsFlow) { orgs, projects ->
            val projectsByOrgId = projects.groupBy { it.organization_id }
            orgs.map { org ->
                OrganizationEntity(
                    id = org.id,
                    name = org.name,
                    slug = org.slug,
                    logoUrl = org.logo_url,
                    projects = projectsByOrgId[org.id].orEmpty().map { project ->
                        ProjectEntity(
                            id = project.id,
                            organizationId = project.organization_id,
                            name = project.name,
                        )
                    },
                )
            }
        }
    }

    override suspend fun replaceAll(organizations: List<OrganizationEntity>) {
        withContext(ioDispatcher) {
            database.transaction {
                database.organizationQueries.deleteAll()
                database.projectQueries.deleteAll()
                organizations.forEach { org ->
                    database.organizationQueries.upsert(
                        id = org.id,
                        name = org.name,
                        slug = org.slug,
                        logo_url = org.logoUrl,
                    )
                    org.projects.forEach { project ->
                        database.projectQueries.upsert(
                            id = project.id,
                            organization_id = org.id,
                            name = project.name,
                        )
                    }
                }
            }
        }
    }
}
