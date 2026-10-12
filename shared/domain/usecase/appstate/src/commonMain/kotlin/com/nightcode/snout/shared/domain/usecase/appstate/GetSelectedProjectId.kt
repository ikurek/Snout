package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import org.koin.core.annotation.Single

/** Project-scoped use cases call this; screens needing it are only reachable once a project is selected. */
@Single
class GetSelectedProjectId(private val repository: AppStateRepository) {
    suspend operator fun invoke(): Long =
        checkNotNull(repository.getSelectedProject()) { "No project selected" }.projectId
}
