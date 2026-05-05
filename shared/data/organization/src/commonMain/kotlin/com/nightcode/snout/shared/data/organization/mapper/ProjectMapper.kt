package com.nightcode.snout.shared.data.organization.mapper

import com.nightcode.snout.shared.api.organizations.model.ProjectDto
import com.nightcode.snout.shared.domain.model.organization.Project
import com.nightcode.snout.shared.storage.database.entity.ProjectEntity

internal fun ProjectDto.toProject(): Project = Project(
    id = id,
    name = name,
)

internal fun ProjectEntity.toProject(): Project = Project(
    id = id,
    name = name,
)
