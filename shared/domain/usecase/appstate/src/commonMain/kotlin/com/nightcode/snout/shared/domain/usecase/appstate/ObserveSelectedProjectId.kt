package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

class ObserveSelectedProjectId(private val repository: AppStateRepository) {
    operator fun invoke(): Flow<Long> = repository.observeSelectedProject()
        .filterNotNull()
        .map { it.projectId }
        .distinctUntilChanged()
}
