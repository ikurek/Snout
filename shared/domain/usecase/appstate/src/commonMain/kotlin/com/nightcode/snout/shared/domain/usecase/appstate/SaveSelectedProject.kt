package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class SaveSelectedProject(private val repository: AppStateRepository) {
    suspend operator fun invoke(organizationId: Uuid, projectId: Long) =
        repository.saveSelectedProject(organizationId, projectId)
}
