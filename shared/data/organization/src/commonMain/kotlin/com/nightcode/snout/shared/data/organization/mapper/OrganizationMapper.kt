package com.nightcode.snout.shared.data.organization.mapper

import com.nightcode.snout.shared.api.organizations.model.OrganizationDto
import com.nightcode.snout.shared.domain.model.organization.Organization
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal fun OrganizationDto.toOrganization(instanceUrl: String): Organization = Organization(
    id = Uuid.parse(id),
    name = name,
    slug = slug,
    logoUrl = logoMediaId?.let { "$instanceUrl/uploaded_media/$it" },
    projects = projects.map { it.toProject() },
)
