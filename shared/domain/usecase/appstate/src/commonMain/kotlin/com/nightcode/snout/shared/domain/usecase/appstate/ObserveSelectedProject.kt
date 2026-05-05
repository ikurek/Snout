package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.appstate.SelectedProject
import kotlinx.coroutines.flow.Flow

class ObserveSelectedProject(private val repository: AppStateRepository) {
    operator fun invoke(): Flow<SelectedProject?> = repository.observeSelectedProject()
}
