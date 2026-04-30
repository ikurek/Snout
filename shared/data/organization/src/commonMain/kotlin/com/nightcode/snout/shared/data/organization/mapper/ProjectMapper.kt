package com.nightcode.snout.shared.data.organization.mapper

import com.nightcode.snout.shared.api.organizations.model.ProjectDto
import com.nightcode.snout.shared.domain.model.organization.Project

internal fun ProjectDto.toProject(): Project = Project(
    id = id,
    name = name,
)
