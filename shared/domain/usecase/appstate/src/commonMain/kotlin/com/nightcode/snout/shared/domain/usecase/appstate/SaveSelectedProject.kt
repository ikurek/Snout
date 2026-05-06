package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import kotlin.uuid.Uuid

class SaveSelectedProject(private val repository: AppStateRepository) {
    suspend operator fun invoke(organizationId: Uuid, projectId: Long) =
        repository.saveSelectedProject(organizationId, projectId)
}
