package com.nightcode.snout.shared.domain.model.appstate

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class SelectedProject(val organizationId: Uuid, val projectId: Long)
