package com.nightcode.snout.shared.domain.model.appstate

import com.nightcode.snout.shared.common.testdata.TestData
import kotlin.uuid.Uuid

data class SelectedProject(val organizationId: Uuid, val projectId: Long)

@TestData
val TestSelectedProject = SelectedProject(
    organizationId = Uuid.parse("00000000-0000-0000-0000-000000000001"),
    projectId = 1L,
)
