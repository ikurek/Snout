package com.nightcode.snout.shared.data.organization.mapper

import com.nightcode.snout.shared.api.organizations.model.OrganizationDto
import com.nightcode.snout.shared.domain.model.organization.Organization
import com.nightcode.snout.shared.storage.database.entity.OrganizationEntity
import com.nightcode.snout.shared.storage.database.entity.ProjectEntity
import kotlin.uuid.Uuid

internal fun OrganizationDto.toOrganization(instanceUrl: String): Organization = Organization(
    id = Uuid.parse(id),
    name = name,
    slug = slug,
    logoUrl = logoMediaId?.let { "$instanceUrl/uploaded_media/$it" },
    projects = projects.map { it.toProject() },
)

internal fun OrganizationEntity.toOrganization(): Organization = Organization(
    id = Uuid.parse(id),
    name = name,
    slug = slug,
    logoUrl = logoUrl,
    projects = projects.map { it.toProject() },
)

internal fun Organization.toEntity(): OrganizationEntity = OrganizationEntity(
    id = id.toString(),
    name = name,
    slug = slug,
    logoUrl = logoUrl,
    projects = projects.map {
        ProjectEntity(
            id = it.id,
            organizationId = id.toString(),
            name = it.name,
        )
    },
)
